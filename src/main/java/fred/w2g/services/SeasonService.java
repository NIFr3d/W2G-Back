package fred.w2g.services;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fred.w2g.entities.Season;
import fred.w2g.entities.Serie;
import fred.w2g.entities.Video;
import fred.w2g.exceptions.CustomException;
import fred.w2g.models.EpisodesUploadRequest;
import fred.w2g.repositories.SeasonRepository;
import fred.w2g.repositories.SerieRepository;
import fred.w2g.repositories.VideoRepository;
import fred.w2g.utils.Constants;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeasonService {

  private final SeasonRepository seasonRepository;
  private final SerieRepository serieRepository;
  private final VideoService videoService;
  private final VideoRepository videoRepository;

  @Transactional
  public void createSeason(Long serieId) {
    Serie serie = serieRepository.findById(serieId)
        .orElseThrow(() -> new CustomException(Constants.SERIE_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND));

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
        .orElseThrow(() -> new CustomException(Constants.SERIE_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND));
    return seasonRepository.findBySerie(serie);
  }

  @Transactional(readOnly = true)
  public Season getSeason(Long id) {
    return seasonRepository.findById(id)
        .orElseThrow(() -> new CustomException(Constants.SEASON_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public void deleteSeasonById(Long id) {
    Season season = seasonRepository.findById(id)
        .orElseThrow(() -> new CustomException(Constants.SEASON_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND));
    deleteSeason(season);
  }

  @Transactional
  public Season updateSeason(Long id, int number) {
    Season season = seasonRepository.findById(id)
        .orElseThrow(() -> new CustomException(Constants.SEASON_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND));
    season.setNumber(number);
    return seasonRepository.save(season);
  }

  @Async
  @Transactional
  public void addVideosToSeason(Long serieId, int seasonNumber, EpisodesUploadRequest request) {
    Serie serie = serieRepository.findById(serieId)
        .orElseThrow(() -> new CustomException(Constants.SERIE_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND));
    Season season = seasonRepository.findBySerieAndNumber(serie, seasonNumber)
        .orElseThrow(() -> new CustomException(Constants.SEASON_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND));
    if (videoRepository.findBySeason(season).stream()
        .anyMatch(video -> video.getEpisode() == request.getEpisodeStart())) {
      throw new CustomException(Constants.EPISODE_EXISTS_ERROR, HttpStatus.FORBIDDEN);
    }
    videoService.uploadVideos(request.getFiles(), season, request.getEpisodeStart());
  }

  @Transactional
  public void deleteVideoFromSeason(Long seasonId, Long videoId) {
    Season season = seasonRepository.findById(seasonId)
        .orElseThrow(() -> new CustomException(Constants.SEASON_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND));
    Video video = videoRepository.findById(videoId)
        .orElseThrow(() -> new CustomException(Constants.VIDEO_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND));
    if (!video.getSeason().equals(season)) {
      throw new CustomException(Constants.VIDEO_NOT_IN_SEASON_ERROR, HttpStatus.FORBIDDEN);
    }
    videoService.deleteVideo(video);
  }

  public void deleteSeason(Season season) {
    season.getVideos().forEach(videoService::deleteVideo);
    seasonRepository.delete(season);
  }
}
