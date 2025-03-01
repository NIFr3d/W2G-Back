package fred.w2g.services;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fred.w2g.entities.Season;
import fred.w2g.entities.Video;
import fred.w2g.exceptions.CustomException;
import fred.w2g.models.ConversionTask;
import fred.w2g.repositories.VideoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import fred.w2g.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoService {

  private final VideoRepository videoRepository;

  private final String tempDirectory = System.getenv().getOrDefault("TEMP_DIRECTORY", "uploads/temp");
  private final String videoDirectory = System.getenv().getOrDefault("VIDEO_DIRECTORY", "uploads/videos");
  private final String subsDirectory = System.getenv().getOrDefault("SUBS_DIRECTORY", "uploads/subs");

  @PostConstruct
  public void init() {
    Utils.createDirectory(tempDirectory);
    Utils.createDirectory(videoDirectory);
    Utils.createDirectory(subsDirectory);
  }

  private final ExecutorService executorService = Executors.newFixedThreadPool(20); // Pool de threads
  private final Map<UUID, ConversionTask> tasks = new ConcurrentHashMap<>(); // Statuts des tâches

  public List<ConversionTask> uploadVideos(List<MultipartFile> files, Season season, float episodeStart) {
    if(files.size() + tasks.size() > 20) {
      throw new CustomException("Too many conversion tasks", HttpStatus.TOO_MANY_REQUESTS);
    }
    List<ConversionTask> taskList = new ArrayList<>();
    float episodeNumber = episodeStart; 
    for (MultipartFile file : files) {
      final float episodeNumberFinal = episodeNumber;
      UUID taskId = UUID.randomUUID();
      ConversionTask task = ConversionTask.builder()
          .taskId(taskId)
          .status("En attente")
          .episodeNumber(episodeNumber)
          .season(season.getNumber())
          .serie(season.getSerie().getTitle())
          .progress(0)
          .build();
      tasks.put(taskId, task);
      // Lancer le traitement asynchrone pour ce fichier
      executorService.submit(() -> uploadVideo(file, season, episodeNumberFinal, task));
      taskList.add(task);
      episodeNumber++;
    }
    return taskList;
  }

  @Async
  public void uploadVideo(MultipartFile videoFile, Season season, float episodeNumber, ConversionTask task) {
    if (!videoRepository.findBySeasonAndEpisode(season, episodeNumber).isEmpty()) {
      throw new CustomException("Episode already exists", HttpStatus.CONFLICT);
    }
    String originalFileName = videoFile.getOriginalFilename();
    String originalExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
    String randomFileName = generateRandomFileName();
    String tempFilePath = tempDirectory + "/" + randomFileName + "." + originalExtension;
    String mp4FileName = randomFileName.replaceFirst("[.][^.]+$", "") + ".mp4";
    String videoFilePath = videoDirectory + "/" + mp4FileName;
    String subFileName = randomFileName + ".srt";
    String subFilePath = subsDirectory + "/" + subFileName;

    Video videoEntity = createVideoInDB(episodeNumber, season, randomFileName);

    try {
      // Copier le fichier dans le répertoire temporaire
      Files.copy(videoFile.getInputStream(), new File(tempFilePath).toPath());
      log.info("File uploaded: " + tempFilePath);

      // Obtenir la durée de la vidéo
      List<String> commandDuration = Arrays.asList(
        "ffprobe", "-v", "error", "-select_streams", "v:0", "-count_packets", "-show_entries", "stream=nb_read_packets", "-of", "csv=p=0",
        tempFilePath
      );

      ProcessBuilder pbDuration = new ProcessBuilder(commandDuration);
      pbDuration.redirectErrorStream(true);
      Process processDuration = pbDuration.start();

      int frames;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(processDuration.getInputStream()))) {
        frames = Integer.parseInt(reader.readLine());
      }
      processDuration.waitFor();

      // Commandes ffmpeg pour convertir le fichier en mp4
      List<String> commandVideo = Arrays.asList(
        "ffmpeg", "-i", tempFilePath,
        videoFilePath
      );

      // Commandes ffmpeg pour extraire les sous-titres
      List<String> commandSubs = Arrays.asList(
        "ffmpeg", "-i", tempFilePath,
        "-map", " 0:s:m:language:fre", subFilePath
      );

      task.setStatus("En cours");

      ProcessBuilder pbVideo = new ProcessBuilder(commandVideo);
      pbVideo.redirectErrorStream(true);
      Process processVideo = pbVideo.start();

      // Lire la sortie de la commande ffmpeg pour mettre à jour la progression
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(processVideo.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
            task.setProgress(calculateProgress(line, frames));
        }
      }
      int exitCodeVideo = processVideo.waitFor();

      // Lancer l'extraction des sous-titres si la vidéo a bien été convertie
      if (exitCodeVideo == 0) {
        ProcessBuilder pbSubs = new ProcessBuilder(commandSubs);
        pbSubs.redirectErrorStream(true);
        Process processSubs = pbSubs.start();
        processSubs.waitFor();
        task.setProgress(100);
        task.setStatus("Terminé");
      } else {
          task.setStatus("Erreur");
      }

      // Supprimer la tâche de la liste
      tasks.remove(task.getTaskId());

      // Supprimer le fichier temporaire
      Files.deleteIfExists(new File(tempFilePath).toPath());
    } catch (Exception e) {
      task.setStatus("Erreur");
      e.printStackTrace();
      deleteVideo(videoEntity);
  }

  }

  @Transactional(readOnly = true)
  public Video getVideo(Long videoId) {
    return videoRepository.findById(videoId)
        .orElseThrow(() -> new CustomException("Video does not exist", HttpStatus.NOT_FOUND));
  }

  @Transactional
  public void deleteVideo(Video video) {
    try {
      Files.deleteIfExists(Paths.get(videoDirectory, video.getFilename() + ".mp4"));
      Files.deleteIfExists(Paths.get(subsDirectory, video.getFilename() + ".srt"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    videoRepository.delete(video);
  }

  private String generateRandomFileName() {
    while (true) {
      String randomString = UUID.randomUUID().toString();
      String randomFileName = randomString + ".mp4";
      if (!videoRepository.findByFilename(randomFileName).isPresent()) {
        return randomString;
      }
    }
  }


  private Video createVideoInDB(float episodeNumber, Season season, String mp4fileName) {
    Video videoEntity = new Video();
    videoEntity.setSeason(season);
    videoEntity.setEpisode(episodeNumber);
    videoEntity.setTitle("Episode " + episodeNumber);
    videoEntity.setDescription("Season " + season.getNumber() + " Episode " + episodeNumber);
    videoEntity.setFilename(mp4fileName);
    return videoRepository.save(videoEntity);
  }

  private int calculateProgress(String line, int frames) {
    if (line.contains("frame=")) {
      int frame = Integer.parseInt(line.substring(line.indexOf("frame=") + 6, line.indexOf("fps") - 1).replace(" ", ""));
      return (int) ((float) frame / frames * 100);
    }
    return 0;
  }

  public List<ConversionTask> getTasks() {
    return new ArrayList<>(tasks.values());
  }

}
