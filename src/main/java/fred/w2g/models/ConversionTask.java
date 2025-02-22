package fred.w2g.models;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversionTask {
  private UUID taskId;
  private String status;
  private float episodeNumber;
  private int season;
  private String serie;
  private int progress;
}
