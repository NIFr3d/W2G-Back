package fred.w2g.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fred.w2g.entities.Season;
import fred.w2g.entities.Serie;
import fred.w2g.exceptions.CustomException;
import fred.w2g.repositories.SeasonRepository;
import fred.w2g.repositories.SerieRepository;

@Service
public class SeasonService {
  @Autowired
  private SeasonRepository seasonRepository;

  @Autowired
  SerieRepository serieRepository;

  @Transactional
  public void createSeason(Long serieId) {
    Serie serie = serieRepository.findById(serieId)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));

    Optional<Integer> maxNumber = seasonRepository.findMaxNumberBySerie(serie);
    int number = maxNumber.orElse(0) + 1;
    Season season = new Season();
    season.setSerie(serie);
    season.setNumber(number);
    seasonRepository.save(season);
  }

  @Transactional(readOnly = true)
  public List<Season> getSeasons(Long serieId) {
    Serie serie = serieRepository.findById(serieId)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));
    return seasonRepository.findBySerie(serie);
  }

  @Transactional(readOnly = true)
  public Season getSeason(Long id) {
    return seasonRepository.findById(id)
        .orElseThrow(() -> new CustomException("Season does not exist", HttpStatus.NOT_FOUND));
  }

  @Transactional
  public void deleteSeason(Season season) {
    // TODO : Delete all videos in the season, including files
    seasonRepository.delete(season);

  }

  @Transactional
  public Season updateSeason(Long id, int number) {
    Season season = seasonRepository.findById(id)
        .orElseThrow(() -> new CustomException("Season does not exist", HttpStatus.NOT_FOUND));
    season.setNumber(number);
    return seasonRepository.save(season);
  }
}
