package fred.w2g.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Utils {
  public static void createDirectory(String path) {
    File directory = new File(path);
    if (!directory.exists()) {
      directory.mkdirs();
    }
  }

  public static byte[] readFile(String path) {
    File file = new File(path);
    if (!file.exists()) {
      throw new RuntimeException("File not found");
    }
    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] data = new byte[(int) file.length()];
      fis.read(data);
      return data;
    } catch (IOException e) {
      throw new RuntimeException("Error reading file", e);
    }

  }
}
