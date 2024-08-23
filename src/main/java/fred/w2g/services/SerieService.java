package fred.w2g.services;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fred.w2g.entities.Serie;
import fred.w2g.entities.Video;
import fred.w2g.exceptions.CustomException;
import fred.w2g.repositories.SerieRepository;

@Service
public class SerieService {
  @Autowired
  private SerieRepository serieRepository;

  @Autowired
  private VideoService videoService;

  @Transactional
  public Serie createSerie(String title, String description) {
    Serie serie = new Serie();
    serie.setTitle(title);
    serie.setDescription(description);

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
}
