package fred.w2g.models;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class EpisodesUploadRequest {

  private float episodeStart;

  private List<MultipartFile> files;
}
