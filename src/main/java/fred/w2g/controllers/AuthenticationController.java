package fred.w2g.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import fred.w2g.models.LoginResponse;

import fred.w2g.entities.User;
import fred.w2g.services.AuthenticationService;
import fred.w2g.services.JwtService;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
  private final JwtService jwtService;

  private final AuthenticationService authenticationService;

  public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
    this.jwtService = jwtService;
    this.authenticationService = authenticationService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> authenticate(@RequestBody User loginRequest) {
    User authenticatedUser = authenticationService.authenticate(loginRequest);

    String jwtToken = jwtService.generateToken(authenticatedUser);

    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setUsername(authenticatedUser.getUsername());
    loginResponse.setRole(authenticatedUser.getRole());
    loginResponse.setToken(jwtToken);
    loginResponse.setExpiresIn(jwtService.getExpirationTime());

    return ResponseEntity.ok(loginResponse);
  }
}