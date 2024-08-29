package fred.w2g.entities;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Serie {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String title;

  private String description;

  private String imageUrl;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "serie", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private Set<Season> seasons;

}
