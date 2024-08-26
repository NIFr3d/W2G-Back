package fred.w2g.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fred.w2g.entities.Serie;
import fred.w2g.entities.Video;
import fred.w2g.exceptions.CustomException;
import fred.w2g.repositories.SerieRepository;
import fred.w2g.repositories.VideoRepository;
import jakarta.annotation.PostConstruct;
import fred.w2g.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class VideoService {
  @Autowired
  private SerieRepository serieRepository;

  @Autowired
  private VideoRepository videoRepository;

  @PostConstruct
  public void init() {
    Utils.createDirectory("uploads/temp");
    Utils.createDirectory("uploads/videos");
  }

  @Transactional
  public Video uploadVideo(MultipartFile videoFile, Long serieId, int seasonNumber, int episodeNumber) {
    Optional<Serie> serie = serieRepository.findById(serieId);
    if (!serie.isPresent()) {
      throw new CustomException("Serie does not exist", HttpStatus.NOT_FOUND);
    }
    if (seasonNumber < 1 || episodeNumber < 1) {
      throw new CustomException("Invalid season or episode number", HttpStatus.BAD_REQUEST);
    }
    if (!videoRepository.findBySerieAndSeasonAndEpisode(serie.get(), seasonNumber, episodeNumber).isEmpty()) {
      throw new CustomException("Episode already exists", HttpStatus.CONFLICT);
    }

    String originalFileName = videoFile.getOriginalFilename();
    String originalExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
    String randomFileName = generateRandomFileName();
    String tempFilePath = "uploads/temp/" + randomFileName + "." + originalExtension;
    String webmFileName = randomFileName.replaceFirst("[.][^.]+$", "") + ".webm";
    String videoFilePath = "uploads/videos/" + webmFileName;

    try {
      // Save the uploaded file temporarily
      saveVideoFile(videoFile, tempFilePath);
      // Convert the video to WebM format
      convertToWebM(tempFilePath, videoFilePath);

      // Delete the temporary file
      new File(tempFilePath).delete();
    } catch (IOException | InterruptedException e) {
      throw new CustomException("Failed to upload video", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    Video videoEntity = new Video();
    videoEntity.setSerie(serie.get());
    videoEntity.setSeason(seasonNumber);
    videoEntity.setEpisode(episodeNumber);
    videoEntity.setTitle("Episode " + episodeNumber);
    videoEntity.setDescription("Season " + seasonNumber + " Episode " + episodeNumber);
    videoEntity.setFilename(webmFileName);

    return videoRepository.save(videoEntity);
  }

  @Transactional(readOnly = true)
  public Video getVideo(Long videoId) {
    return videoRepository.findById(videoId)
        .orElseThrow(() -> new CustomException("Video does not exist", HttpStatus.NOT_FOUND));
  }

  private String generateRandomFileName() {
    while (true) {
      String randomString = UUID.randomUUID().toString();
      String randomFileName = randomString + ".webm";
      if (!videoRepository.findByFilename(randomFileName).isPresent()) {
        return randomString;
      }
    }
  }

  private void saveVideoFile(MultipartFile videoFile, String filePath) throws IOException {
    File file = new File(filePath);
    Files.copy(videoFile.getInputStream(), file.toPath());
  }

  private void convertToWebM(String inputFilePath, String outputFilePath) throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(
        "ffmpeg", "-i", inputFilePath, outputFilePath);
    Process process = processBuilder.start();
    process.waitFor();
  }

  @Transactional
  public void deleteVideosForSerie(Serie serie) {
    Set<Video> videos = videoRepository.findBySerie(serie);
    for (Video video : videos) {
      String videoFilePath = "uploads/videos/" + video.getFilename();
      new File(videoFilePath).delete();
      videoRepository.delete(video);
    }
  }

  @Transactional(readOnly = true)
  public Set<Integer> getSeasonsForSerie(Serie serie) {
    return videoRepository.findDistinctSeasonsBySerie(serie);
  }
}
