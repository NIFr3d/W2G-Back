package fred.w2g.listeners;

import java.io.File;

import fred.w2g.entities.Video;
import jakarta.persistence.PreRemove;

public class VideoEntityListener {

  private final String videoDirectory = System.getenv().getOrDefault("VIDEO_DIRECTORY", "uploads/videos");

  @PreRemove
  public void preRemove(Video video) {
    // Supprimer le fichier associé
    File videoFile = new File(videoDirectory + "/" + video.getFilename());
    if (videoFile.exists()) {
      videoFile.delete();
    }
  }
}
