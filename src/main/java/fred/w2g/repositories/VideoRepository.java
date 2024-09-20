package fred.w2g.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fred.w2g.entities.Season;
import fred.w2g.entities.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
  public Optional<Video> findByFilename(String filename);

  public Optional<Video> findBySeasonAndEpisode(Season season, float episodeNumber);

  public List<Video> findBySeason(Season season);

}
