package fred.w2g.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fred.w2g.entities.Season;
import fred.w2g.entities.Serie;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
  Optional<Season> findBySerieAndNumber(Serie serie, int number);

  List<Season> findBySerie(Serie serie);

  default Optional<Integer> findMaxNumberBySerie(Serie serie) {
    return findAll().stream()
        .filter(season -> season.getSerie().equals(serie))
        .map(Season::getNumber)
        .max(Integer::compareTo);
  }

}
