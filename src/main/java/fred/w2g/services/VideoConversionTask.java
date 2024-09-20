package fred.w2g.services;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import java.io.File;

public class VideoConversionTask implements Runnable {
  private final String inputFilePath;
  private final String outputFilePath;
  private final String taskId;
  private final VideoService service;

  public VideoConversionTask(String taskId, String inputFilePath, String outputFilePath,
      VideoService service) {
    this.taskId = taskId;
    this.inputFilePath = inputFilePath;
    this.outputFilePath = outputFilePath;
    this.service = service;
  }

  @Override
  public void run() {
    service.updateStatus(taskId, "PROCESSING");
    try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath)) {
      grabber.start();

      // Configurer l'enregistreur
      FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, grabber.getImageWidth(),
          grabber.getImageHeight(), grabber.getAudioChannels());
      recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // Utiliser le codec H.264 pour la vidéo
      recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // Utiliser le codec AAC pour l'audio
      recorder.setFormat("mp4"); // Format de sortie MP4

      // Paramètres de qualité vidéo
      recorder.setFrameRate(grabber.getFrameRate()); // Frame rate du fichier source
      recorder.setVideoBitrate(4000 * 1000); // Bitrate vidéo augmenté à 4000 kbps pour une meilleure qualité
      recorder.setAudioBitrate(192 * 1000); // Bitrate audio à 192 kbps
      recorder.setSampleRate(grabber.getSampleRate()); // Garder la même fréquence d'échantillonnage

      // Options supplémentaires pour H.264
      recorder.setVideoOption("preset", "slow"); // Choisir un preset lent pour une meilleure compression
      recorder.setVideoOption("crf", "23"); // Le CRF pour une bonne qualité (valeur par défaut est généralement 23)
      recorder.setVideoOption("profile", "main"); // Utiliser le profil Main pour H.264

      recorder.start();

      Frame frame;
      while ((frame = grabber.grab()) != null) {
        recorder.record(frame); // Enregistrer chaque frame
      }

      recorder.stop();
      grabber.stop();
      service.updateStatus(taskId, "COMPLETED");

      // Nettoyer les ressources
      if (recorder != null) {
        recorder.release();
      }
      if (grabber != null) {
        grabber.release();
      }

    } catch (Exception e) {
      e.printStackTrace();
      service.updateStatus(taskId, "FAILED");

    } finally {
      new File(inputFilePath).delete(); // Supprime le fichier temporaire
    }
  }
}
