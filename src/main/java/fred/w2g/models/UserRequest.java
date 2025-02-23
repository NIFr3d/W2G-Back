package fred.w2g.models;

import lombok.Data;

@Data
public class UserRequest {
  private String username;
  private String password;
  private String role;
}
