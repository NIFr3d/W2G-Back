package fred.w2g.utils;

public class Constants {
  /**
   * Private constructor to hide the implicit public one.
   * This class should not be instantiated as it only contains constants.
   */
  private Constants() {
    throw new IllegalStateException("Utility class");
  }

  public static final String SERIE_NOT_FOUND_ERROR = "Serie does not exist";
  public static final String SEASON_NOT_FOUND_ERROR = "Season does not exist";
  public static final String VIDEO_NOT_FOUND_ERROR = "Video does not exist";
  public static final String EPISODE_EXISTS_ERROR = "Episode already exists in the season";
  public static final String VIDEO_NOT_IN_SEASON_ERROR = "Video does not belong to the season";
}
