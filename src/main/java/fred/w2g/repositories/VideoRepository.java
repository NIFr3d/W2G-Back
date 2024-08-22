package fred.w2g.repositories;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fred.w2g.entities.Show;
import fred.w2g.entities.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
  public Optional<Video> findByFilename(String filename);

  public Set<Video> findByShowAndSeasonAndEpisode(Show show, int seasonNumber, int episodeNumber);

  public Set<Video> findByShow(Show show);
}
