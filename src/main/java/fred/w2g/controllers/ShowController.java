package fred.w2g.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fred.w2g.entities.Show;
import fred.w2g.services.ShowService;

@RestController
@RequestMapping("/show")
public class ShowController {
  @Autowired
  private ShowService showService;

  @GetMapping
  public List<Show> getShows() {
    return showService.getShows();
  }

  @PostMapping
  public Show createShow(@RequestParam String title, @RequestParam String description) {
    return showService.createShow(title, description);
  }

  @GetMapping("/{id}")
  public Show getShow(@PathVariable Long id) {
    return showService.getShow(id);
  }

  @DeleteMapping("/{id}")
  public void deleteShow(@PathVariable Long id) {
    showService.deleteShow(id);
  }
}
