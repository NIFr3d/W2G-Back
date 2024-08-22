package fred.w2g.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fred.w2g.entities.Serie;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {

}
