package fred.w2g.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fred.w2g.entities.Season;
import fred.w2g.entities.Serie;
import fred.w2g.entities.Video;
import fred.w2g.exceptions.CustomException;
import fred.w2g.models.EpisodesUploadRequest;
import fred.w2g.repositories.SeasonRepository;
import fred.w2g.repositories.SerieRepository;
import fred.w2g.repositories.VideoRepository;

@Service
public class SeasonService {
  @Autowired
  private SeasonRepository seasonRepository;

  @Autowired
  private SerieRepository serieRepository;

  @Autowired
  private VideoService videoService;

  @Autowired
  private VideoRepository videoRepository;

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
    seasonRepository.delete(season);

  }

  @Transactional
  public Season updateSeason(Long id, int number) {
    Season season = seasonRepository.findById(id)
        .orElseThrow(() -> new CustomException("Season does not exist", HttpStatus.NOT_FOUND));
    season.setNumber(number);
    return seasonRepository.save(season);
  }

  @Async
  @Transactional
  public void addVideosToSeason(Long serieId, int seasonNumber, EpisodesUploadRequest request) {
    Serie serie = serieRepository.findById(serieId)
        .orElseThrow(() -> new CustomException("Serie does not exist", HttpStatus.NOT_FOUND));
    Season season = seasonRepository.findBySerieAndNumber(serie, seasonNumber)
        .orElseThrow(() -> new CustomException("Season does not exist", HttpStatus.NOT_FOUND));
    if (videoRepository.findBySeason(season).stream()
        .anyMatch(video -> video.getEpisode() == request.getEpisodeStart())) {
      throw new CustomException("Episode already exists in the season", HttpStatus.FORBIDDEN);
    }
    List<MultipartFile> files = request.getFiles();
    for (int i = 0; i < files.size(); i++) {
      videoService.uploadVideo(files.get(i), season, request.getEpisodeStart() + i);
    }
  }

  @Transactional
  public void deleteVideoFromSeason(Long seasonId, Long videoId) {
    Season season = seasonRepository.findById(seasonId)
        .orElseThrow(() -> new CustomException("Season does not exist", HttpStatus.NOT_FOUND));
    Video video = videoRepository.findById(videoId)
        .orElseThrow(() -> new CustomException("Video does not exist", HttpStatus.NOT_FOUND));
    if (!video.getSeason().equals(season)) {
      throw new CustomException("Video does not belong to the season", HttpStatus.FORBIDDEN);
    }
    videoService.deleteVideo(video);
  }
}
