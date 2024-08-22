package fred.w2g.models;

import lombok.Data;

@Data
public class LoginResponse {
  private String username;

  private String role;

  private String token;

  private long expiresIn;

}
