package fred.w2g.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import fred.w2g.entities.User;
import fred.w2g.exceptions.CustomException;
import fred.w2g.repositories.UserRepository;

@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;

  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new CustomException("User does not exist", HttpStatus.NOT_FOUND));
  }

}
