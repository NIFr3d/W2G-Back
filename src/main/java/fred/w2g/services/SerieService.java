package fred.w2g.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fred.w2g.entities.Serie;
import fred.w2g.exceptions.CustomException;
import fred.w2g.repositories.SerieRepository;
import fred.w2g.utils.Utils;
import jakarta.annotation.PostConstruct;

@Service
public class SerieService {
  @Autowired
  private SerieRepository serieRepository;

  @Autowired
  private VideoService videoService;

  @PostConstruct
  public void init() {
    Utils.createDirectory("uploads/images");
  }

  @Transactional
  public Serie createSerie(String title, String description, MultipartFile thumbnail) {

    if (!isImage(thumbnail)) {
      throw new CustomException("Invalid file type", HttpStatus.BAD_REQUEST);
    }

    if (title == null || title.isEmpty()) {
      throw new CustomException("Title is required", HttpStatus.BAD_REQUEST);
    }
    if (serieRepository.findByTitle(title).isPresent()) {
      throw new CustomException("Serie already exists", HttpStatus.CONFLICT);
    }

    String uniqueFileName = UUID.randomUUID().toString() + ".webp";
    File outputFile = new File("uploads/images/" + uniqueFileName);

    try {
      BufferedImage bufferedImage = ImageIO.read(thumbnail.getInputStream());
      saveAsWebP(bufferedImage, outputFile);
    } catch (IOException e) {
      throw new CustomException("Error processing image", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    Serie serie = new Serie();
    serie.setTitle(title);
    serie.setDescription(description);
    serie.setImageUrl(outputFile.getPath());

    return serieRepository.save(serie);
  }

  private boolean isImage(MultipartFile file) {
    try {
      BufferedImage image = ImageIO.read(file.getInputStream());
      return image != null;
    } catch (IOException e) {
      return false;
    }
  }

  private void saveAsWebP(BufferedImage image, File outputFile) throws IOException {
    ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
      writer.setOutput(ios);
      writer.write(image);
    } finally {
      writer.dispose();
    }
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
  public void deleteSerie(Long id) {
    Serie serie = serieRepository.findById(id)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));

    videoService.deleteVideosForSerie(serie);
    serieRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public Set<Integer> getSeasons(Long id) {
    Serie serie = serieRepository.findById(id)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));

    return videoService.getSeasonsForSerie(serie);
  }

  @Transactional(readOnly = true)
  public byte[] getThumbnail(Long id) {
    Serie serie = serieRepository.findById(id)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));

    return Utils.readFile(serie.getImageUrl());
  }
}
