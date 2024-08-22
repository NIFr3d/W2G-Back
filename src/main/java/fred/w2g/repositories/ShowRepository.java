package fred.w2g.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fred.w2g.entities.Show;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

}
