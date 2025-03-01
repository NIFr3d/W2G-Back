package fred.w2g.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import fred.w2g.entities.User;
import fred.w2g.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;

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