package fred.w2g.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fred.w2g.entities.User;
import fred.w2g.models.UserRequest;
import fred.w2g.services.UserService;
import lombok.RequiredArgsConstructor;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  @PostMapping
  public User createUser(@RequestBody UserRequest request) {
    return userService.createUser(request);
  }

  @PutMapping("/{id}")
  public User updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
    return userService.updateUser(id, request);
  }

  @DeleteMapping("/{id}")
  public void deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
  }
}
