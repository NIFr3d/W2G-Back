package fred.w2g.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fred.w2g.entities.Video;
import fred.w2g.services.VideoService;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
public class VideoController {

  private final VideoService videoService;

  @GetMapping("/video/{id}")
  public ResponseEntity<Resource> getVideo(@PathVariable("id") Long videoId) {
    Video videoEntity = videoService.getVideo(videoId);
    Path videoPath = Paths.get(String.format("uploads/videos/%s.mp4", videoEntity.getFilename()));
    Resource videoResource = new PathResource(videoPath);
    
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("video/mp4"))
        .body(videoResource);
  }

  @GetMapping("/subtitles/{id}")
  public ResponseEntity<Resource> getSubtitles(@PathVariable("id") Long videoId) {
    Video videoEntity = videoService.getVideo(videoId);
    Path subtitlesPath = Paths.get(String.format("uploads/subs/%s.ass", videoEntity.getFilename()));
    Resource subtitlesResource = new PathResource(subtitlesPath);
    
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/ass"))
        .body(subtitlesResource);
  }

  @GetMapping("/episode/{id}")
  public Video getEpisode(@PathVariable("id") Long episodeId) {
    return videoService.getVideo(episodeId);
  }
}
