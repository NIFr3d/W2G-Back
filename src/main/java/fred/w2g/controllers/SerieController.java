package fred.w2g.controllers;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fred.w2g.entities.Serie;
import fred.w2g.services.SerieService;

@RestController
@RequestMapping("/serie")
public class SerieController {
  @Autowired
  private SerieService serieService;

  @GetMapping
  public List<Serie> getSeries() {
    return serieService.getSeries();
  }

  @PostMapping
  public Serie createSerie(@RequestPart("title") String title, @RequestPart("description") String description,
      @RequestPart("thumbnail") MultipartFile thumbnail) {
    System.out.println(thumbnail.getOriginalFilename()); // TODO : save thumbnail
    return serieService.createSerie(title, description);
  }

  @GetMapping("/{id}")
  public Serie getSerie(@PathVariable Long id) {
    return serieService.getSerie(id);
  }

  @GetMapping("/{id}/seasons")
  public Set<Integer> getSeasons(@PathVariable Long id) {
    return serieService.getSeasons(id);
  }

  @DeleteMapping("/{id}")
  public void deleteSerie(@PathVariable Long id) {
    serieService.deleteSerie(id);
  }
}
