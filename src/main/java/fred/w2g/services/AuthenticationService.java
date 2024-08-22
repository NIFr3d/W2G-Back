package fred.w2g.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import fred.w2g.entities.User;
import fred.w2g.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
  private final UserRepository userRepository;

  private final AuthenticationManager authenticationManager;

  public AuthenticationService(
      UserRepository userRepository,
      AuthenticationManager authenticationManager,
      PasswordEncoder passwordEncoder) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public User authenticate(User input) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            input.getUsername(),
            input.getPassword()));

    return userRepository.findByUsername(input.getUsername())
        .orElseThrow();
  }
}