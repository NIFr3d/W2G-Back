package fred.w2g.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fred.w2g.entities.Season;
import fred.w2g.entities.Video;
import fred.w2g.exceptions.CustomException;
import fred.w2g.repositories.VideoRepository;
import jakarta.annotation.PostConstruct;
import fred.w2g.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.UUID;

@Service
public class VideoService {
  @Autowired
  private VideoRepository videoRepository;

  private final String tempDirectory = System.getenv().getOrDefault("TEMP_DIRECTORY", "uploads/temp");
  private final String videoDirectory = System.getenv().getOrDefault("VIDEO_DIRECTORY", "uploads/videos");

  @PostConstruct
  public void init() {
    Utils.createDirectory(tempDirectory);
    Utils.createDirectory(videoDirectory);
  }

  private final ExecutorService executorService = Executors.newFixedThreadPool(4); // Pool de threads
  private final Map<String, String> taskStatus = new ConcurrentHashMap<>(); // Statuts des tâches

  @Async
  public void uploadVideo(MultipartFile videoFile, Season season, float episodeNumber) {
    if (!videoRepository.findBySeasonAndEpisode(season, episodeNumber).isEmpty()) {
      throw new CustomException("Episode already exists", HttpStatus.CONFLICT);
    }
    String originalFileName = videoFile.getOriginalFilename();
    String originalExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
    String randomFileName = generateRandomFileName();
    String tempFilePath = tempDirectory + "/" + randomFileName + "." + originalExtension;
    String mp4FileName = randomFileName.replaceFirst("[.][^.]+$", "") + ".mp4";
    String videoFilePath = videoDirectory + "/" + mp4FileName;

    Video videoEntity = createVideoInDB(episodeNumber, season, mp4FileName);

    try {
      conversionSteps(videoFile, tempFilePath, videoFilePath, videoEntity.getId());
    } catch (IOException e) {
      // updateVideoStatus(videoEntity.getId(), "ERROR");
      // TODO: Find a way to update the status without concurrency issues
      throw new CustomException("Error while converting video, IOException", HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (InterruptedException e) {
      // updateVideoStatus(videoEntity.getId(), "ERROR");
      // TODO: Find a way to update the status without concurrency issues
      throw new CustomException("Error while converting video, InterruptedException", HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  @Transactional(readOnly = true)
  public Video getVideo(Long videoId) {
    return videoRepository.findById(videoId)
        .orElseThrow(() -> new CustomException("Video does not exist", HttpStatus.NOT_FOUND));
  }

  @Transactional
  public Video createVideoInDB(float episodeNumber, Season season, String webmFileName) {
    Video videoEntity = new Video();
    videoEntity.setSeason(season);
    videoEntity.setEpisode(episodeNumber);
    videoEntity.setTitle("Episode " + episodeNumber);
    videoEntity.setDescription("Season " + season.getNumber() + " Episode " + episodeNumber);
    videoEntity.setFilename(webmFileName);
    return videoRepository.save(videoEntity);
  }

  // Méthode pour mettre à jour le statut de la tâche
  public void updateStatus(String taskId, String status) {
    taskStatus.put(taskId, status);
  }

  // Méthode pour obtenir le statut d'une tâche
  public String getTaskStatus(String taskId) {
    return taskStatus.getOrDefault(taskId, "unknown");
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

  @Async
  private void conversionSteps(MultipartFile videoFile, String tempFilePath, String videoFilePath, Long videoEntityId)
      throws IOException, InterruptedException {

    // Save the uploaded file temporarily
    File file = new File(tempFilePath);
    Files.copy(videoFile.getInputStream(), file.toPath());

    // Convert the video to WebM format
    // ProcessBuilder processBuilder = new ProcessBuilder(
    // "ffmpeg", "-i", tempFilePath, videoFilePath, "-preset", "ultrafast",
    // "-threads", "8");
    // processManagerService.startProcess(videoEntityId.toString(), processBuilder,
    // tempFilePath);

    taskStatus.put(videoEntityId.toString(), "PENDING");

    VideoConversionTask task = new VideoConversionTask(videoEntityId.toString(), tempFilePath, videoFilePath, this);
    executorService.submit(task);
    taskStatus.put(videoEntityId.toString(), "STARTED");

  }

  @Transactional
  public void deleteVideo(Video video) {
    videoRepository.delete(video);
  }

}
