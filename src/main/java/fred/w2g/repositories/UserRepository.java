package fred.w2g.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fred.w2g.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  public Optional<User> findByUsername(String username);

}
