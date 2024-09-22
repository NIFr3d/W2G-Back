package fred.w2g.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fred.w2g.entities.Season;
import fred.w2g.entities.Serie;
import fred.w2g.entities.Video;
import fred.w2g.models.EpisodesUploadRequest;
import fred.w2g.models.SerieRequest;
import fred.w2g.services.SeasonService;
import fred.w2g.services.SerieService;

@RestController
@RequestMapping("/serie")
public class SerieController {
  @Autowired
  private SerieService serieService;

  @Autowired
  private SeasonService seasonService;

  @GetMapping
  public List<Serie> getSeries() {
    return serieService.getSeries();
  }

  @PostMapping
  public Serie createSerie(@RequestPart("title") String title, @RequestPart("description") String description,
      @RequestPart("thumbnail") MultipartFile thumbnail) {
    return serieService.createSerie(title, description, thumbnail);
  }

  @GetMapping("/{id}")
  public Serie getSerie(@PathVariable Long id) {
    return serieService.getSerie(id);
  }

  @PutMapping("/{id}")
  public Serie updateSerie(@PathVariable Long id, @RequestBody SerieRequest toUpdate) {
    return serieService.updateSerie(id, toUpdate);
  }

  @GetMapping("/{id}/thumbnail")
  public byte[] getThumbnail(@PathVariable Long id) {
    return serieService.getThumbnail(id);
  }

  @PostMapping("/{id}/thumbnail")
  public void setThumbnail(@PathVariable Long id, @RequestPart("thumbnail") MultipartFile thumbnail) {
    serieService.setThumbnail(id, thumbnail);
  }

  @DeleteMapping("/{id}")
  public void deleteSerie(@PathVariable Long id) {
    serieService.deleteSerie(id);
  }

  @GetMapping("/{id}/season")
  public List<Season> getSeasons(@PathVariable Long id) {
    return seasonService.getSeasons(id);
  }

  @PostMapping("/{id}/season")
  public void createSeason(@PathVariable Long id) {
    seasonService.createSeason(id);
  }

  @PutMapping("/{id}/season/{seasonId}")
  public Season updateSeason(@PathVariable Long id, @PathVariable Long seasonId, @RequestParam int number) {
    return seasonService.updateSeason(seasonId, number);
  }

  @DeleteMapping("/{id}/season/{seasonId}")
  public void deleteSeason(@PathVariable Long id, @PathVariable Long seasonId) {
    Season toDelete = seasonService.getSeason(seasonId);
    seasonService.deleteSeason(toDelete);

  }

  @GetMapping("/{id}/video")
  public List<Video> getVideosForSerie(@PathVariable Long id) {
    return serieService.getVideos(id);
  }

  @PostMapping("/{id}/season/{seasonNumber}/video")
  public void addVideosToSeason(@ModelAttribute EpisodesUploadRequest request, @PathVariable Long id,
      @PathVariable int seasonNumber) {
    seasonService.addVideosToSeason(id, seasonNumber, request);
    try { // On attend 2 secondes pour que les fichiers temporaires ne soient pas
          // supprimés avant d'être copiés
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @DeleteMapping("/{id}/season/{seasonId}/video/{videoId}")
  public void deleteVideoFromSeason(@PathVariable Long id, @PathVariable Long seasonId, @PathVariable Long videoId) {
    seasonService.deleteVideoFromSeason(seasonId, videoId);
  }

}
