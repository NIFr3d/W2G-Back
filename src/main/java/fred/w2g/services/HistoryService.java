package fred.w2g.services;

import java.util.List;

import org.springframework.stereotype.Service;

import fred.w2g.entities.User;
import fred.w2g.entities.WatchHistory;
import fred.w2g.repositories.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryService {

  private final WatchHistoryRepository watchHistoryRepository;

  public List<WatchHistory> getHistory(User user) {
    return watchHistoryRepository.findByUser(user);
  }

}
