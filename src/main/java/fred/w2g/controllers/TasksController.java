package fred.w2g.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fred.w2g.models.ConversionTask;
import java.util.List;

import fred.w2g.services.VideoService;

@RequestMapping("/tasks")
@RestController
public class TasksController {
  @Autowired
  private VideoService videoService;

  @GetMapping
  public List<ConversionTask> getTasks() {
    return videoService.getTasks();
  }
  
}
