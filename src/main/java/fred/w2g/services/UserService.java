package fred.w2g.services;

import java.util.List;
import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import fred.w2g.entities.User;
import fred.w2g.exceptions.CustomException;
import fred.w2g.models.UserRequest;
import fred.w2g.repositories.UserRepository;

@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new CustomException("User does not exist", HttpStatus.NOT_FOUND));
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public User createUser(UserRequest request) {
    User user = User.builder()
        .username(request.getUsername())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .build();
    return userRepository.save(user);
  }

  public User updateUser(Long id, UserRequest request) {
    User existingUser = userRepository.findById(id)
        .orElseThrow(() -> new CustomException("User does not exist", HttpStatus.NOT_FOUND));
    existingUser.setUsername(request.getUsername());
    if(request.getPassword().length() > 0) {
      existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
    }
    existingUser.setRole(request.getRole());
    return userRepository.save(existingUser);
  }

  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }

}
