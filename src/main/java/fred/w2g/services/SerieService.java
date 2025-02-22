package fred.w2g.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fred.w2g.entities.Season;
import fred.w2g.entities.Serie;
import fred.w2g.entities.Video;
import fred.w2g.exceptions.CustomException;
import fred.w2g.models.SerieRequest;
import fred.w2g.repositories.SeasonRepository;
import fred.w2g.repositories.SerieRepository;
import fred.w2g.repositories.VideoRepository;
import fred.w2g.utils.Utils;

@Service
public class SerieService {
  @Autowired
  private SerieRepository serieRepository;

  @Autowired
  private SeasonService seasonService;

  @Autowired
  SeasonRepository seasonRepository;

  private final String imageDirectory = System.getenv().getOrDefault("IMAGE_DIRECTORY", "uploads/images");

  @PostConstruct
  public void init() {
    Utils.createDirectory("uploads/images");
  }

  @Transactional
  public Serie createSerie(String title, String description, MultipartFile thumbnail) {

    if (!Utils.isImage(thumbnail)) {
      throw new CustomException("Invalid file type", HttpStatus.BAD_REQUEST);
    }

    if (title == null || title.isEmpty()) {
      throw new CustomException("Title is required", HttpStatus.BAD_REQUEST);
    }
    if (serieRepository.findByTitle(title).isPresent()) {
      throw new CustomException("Serie already exists", HttpStatus.CONFLICT);
    }

    String uniqueFileName = UUID.randomUUID().toString() + ".webp";
    File outputFile = new File(imageDirectory + "/" + uniqueFileName);

    try {
      BufferedImage bufferedImage = ImageIO.read(thumbnail.getInputStream());
      Utils.saveAsWebP(bufferedImage, outputFile);
    } catch (IOException e) {
      throw new CustomException("Error processing image", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    Serie serie = new Serie();
    serie.setTitle(title);
    serie.setDescription(description);
    serie.setImageUrl(uniqueFileName);

    return serieRepository.save(serie);
  }

  @Transactional(readOnly = true)
  public List<Serie> getSeries() {
    return serieRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Serie getSerie(Long id) {
    return serieRepository.findById(id)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));
  }

  @Transactional
  public Serie updateSerie(Long id, SerieRequest toUpdate) {
    Serie serie = serieRepository.findById(id)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));
    if (toUpdate.getTitle() != null && !toUpdate.getTitle().isEmpty()) {
      serie.setTitle(toUpdate.getTitle());
    }
    if (toUpdate.getDescription() != null) {
      serie.setDescription(toUpdate.getDescription());
    }
    return serieRepository.save(serie);
  }

  @Transactional
  public void deleteSerie(Long id) {
    Serie serie = serieRepository.findById(id)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));
    serie.getSeasons().forEach(seasonService::deleteSeason);
    serieRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public byte[] getThumbnail(Long id) {
    Serie serie = serieRepository.findById(id)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));

    return Utils.readFile(imageDirectory + "/" + serie.getImageUrl());
  }

  @Transactional
  public void setThumbnail(Long id, MultipartFile thumbnail) {
    Serie serie = serieRepository.findById(id)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));

    if (!Utils.isImage(thumbnail)) {
      throw new CustomException("Invalid file type", HttpStatus.BAD_REQUEST);
    }

    File outputFile = new File(imageDirectory + "/" + serie.getImageUrl());

    try {
      BufferedImage bufferedImage = ImageIO.read(thumbnail.getInputStream());
      Utils.saveAsWebP(bufferedImage, outputFile);
    } catch (IOException e) {
      throw new CustomException("Error processing image", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Transactional(readOnly = true)
  public List<Video> getVideos(Long id) {

    Serie serie = serieRepository.findById(id)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));
    List<Season> seasons = serie.getSeasons();
    List<Video> videos = new ArrayList<>();

    for (Season season : seasons) {
      synchronized (season) { // Synchronisation pour éviter les problèmes de concurrence
        videos.addAll(season.getVideos()); // Créer une copie de la collection
      }
    }
    return videos;
  }

}
