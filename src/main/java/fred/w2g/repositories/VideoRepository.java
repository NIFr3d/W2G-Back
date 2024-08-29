package fred.w2g.repositories;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fred.w2g.entities.Season;
import fred.w2g.entities.Serie;
import fred.w2g.entities.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
  public Optional<Video> findByFilename(String filename);

  public Set<Video> findBySerieAndSeasonAndEpisode(Serie serie, Season season, float episodeNumber);

  public Set<Video> findBySerie(Serie serie);

  public Set<Integer> findDistinctSeasonsBySerie(Serie serie);
}
