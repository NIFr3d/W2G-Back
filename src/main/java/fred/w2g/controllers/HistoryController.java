package fred.w2g.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fred.w2g.entities.User;
import fred.w2g.entities.WatchHistory;
import fred.w2g.services.HistoryService;
import fred.w2g.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {
  @Autowired
  private HistoryService historyService;

  @Autowired
  private UserService userService;

  @GetMapping
  public List<WatchHistory> getHistory(Authentication authentication) {
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    User user = userService.getUserByUsername(userDetails.getUsername());
    return historyService.getHistory(user);

  }
}
