package fred.w2g.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fred.w2g.entities.Serie;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {
  public Optional<Serie> findByTitle(String title);
  public List<Serie> findByTitleContainingIgnoreCase(String title);
}
