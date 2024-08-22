package fred.w2g.models;

import lombok.Data;

@Data
public class LoginResponse {
  private String token;

  private long expiresIn;

}
