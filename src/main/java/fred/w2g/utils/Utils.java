package fred.w2g.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.web.multipart.MultipartFile;

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

  public static boolean isImage(MultipartFile file) {
    try {
      BufferedImage image = ImageIO.read(file.getInputStream());
      return image != null;
    } catch (IOException e) {
      return false;
    }
  }

  public static void saveAsWebP(BufferedImage image, File outputFile) throws IOException {
    ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
      writer.setOutput(ios);
      writer.write(image);
    } finally {
      writer.dispose();
    }
  }
}
