package fred.w2g.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fred.w2g.listeners.SerieEntityListener;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
@EntityListeners(SerieEntityListener.class)
public class Serie {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String title;

  private String description;

  private String imageUrl;

  @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<Season> seasons;

}
