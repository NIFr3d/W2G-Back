package fred.w2g.entities;

import fred.w2g.listeners.VideoEntityListener;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "season_id", "episode" }))
@EntityListeners(VideoEntityListener.class)
public class Video {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String title;

  private String description;

  @Column(unique = true)
  private String filename;

  @ManyToOne
  @JoinColumn(name = "season_id", nullable = false)
  private Season season;

  private float episode;

}
