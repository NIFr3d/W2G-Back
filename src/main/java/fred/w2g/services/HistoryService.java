package fred.w2g.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fred.w2g.entities.User;
import fred.w2g.entities.WatchHistory;
import fred.w2g.repositories.WatchHistoryRepository;

@Service
public class HistoryService {
  @Autowired
  private WatchHistoryRepository watchHistoryRepository;

  public List<WatchHistory> getHistory(User user) {
    return watchHistoryRepository.findByUser(user);
  }

}
