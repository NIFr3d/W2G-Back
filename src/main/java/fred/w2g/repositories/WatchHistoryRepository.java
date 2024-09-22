package fred.w2g.repositories;

import fred.w2g.entities.User;
import fred.w2g.entities.WatchHistory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

  List<WatchHistory> findByUser(User user);

}
