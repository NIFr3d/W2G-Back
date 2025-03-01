package fred.w2g.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fred.w2g.entities.Video;
import fred.w2g.services.VideoService;
import lombok.RequiredArgsConstructor;

import java.net.MalformedURLException;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

  private final VideoService videoService;

  @GetMapping("/{id}")
  public ResponseEntity<UrlResource> getVideo(@PathVariable("id") Long videoId) throws MalformedURLException {
    Video videoEntity = videoService.getVideo(videoId);
    UrlResource video = new UrlResource(
        String.format("file:uploads/videos/%s", videoEntity.getFilename()));
    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
        .contentType(MediaTypeFactory.getMediaType(video)
            .orElse(MediaType.APPLICATION_OCTET_STREAM))
        .body(video);
  }

}
