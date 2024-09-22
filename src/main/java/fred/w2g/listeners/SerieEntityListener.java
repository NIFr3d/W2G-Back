package fred.w2g.listeners;

import fred.w2g.entities.Serie;

import jakarta.persistence.PreRemove;

import java.io.File;

public class SerieEntityListener {
  private final String imageDirectory = System.getenv().getOrDefault("IMAGE_DIRECTORY", "uploads/images");

  @PreRemove
  public void preRemove(Serie serie) {
    // Supprimer le fichier associé
    File imageFile = new File(imageDirectory + "/" + serie.getImageUrl());
    if (imageFile.exists()) {
      imageFile.delete();
    }
  }

}
