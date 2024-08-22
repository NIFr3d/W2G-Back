package fred.w2g.services;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fred.w2g.entities.Show;
import fred.w2g.entities.Video;
import fred.w2g.exceptions.CustomException;
import fred.w2g.repositories.ShowRepository;

@Service
public class ShowService {
  @Autowired
  private ShowRepository showRepository;

  @Autowired
  private VideoService videoService;

  @Transactional
  public Show createShow(String title, String description) {
    Show show = new Show();
    show.setTitle(title);
    show.setDescription(description);

    return showRepository.save(show);
  }

  @Transactional(readOnly = true)
  public List<Show> getShows() {
    return showRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Show getShow(Long id) {
    return showRepository.findById(id)
        .orElseThrow(() -> new CustomException("Show does not exist", HttpStatus.NOT_FOUND));
  }

  @Transactional
  public void deleteShow(Long id) {
    Show show = showRepository.findById(id)
        .orElseThrow(() -> new CustomException("Show does not exist", HttpStatus.NOT_FOUND));

    videoService.deleteVideosForShow(show);
    showRepository.deleteById(id);
  }
}
