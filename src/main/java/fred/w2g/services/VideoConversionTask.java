package fred.w2g.services;

import org.bytedeco.ffmpeg.global.avcodec;
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
      recorder.setVideoCodec(avcodec.AV_CODEC_ID_VP8); // Choisir le codec vidéo
      recorder.setAudioCodec(avcodec.AV_CODEC_ID_VORBIS); // Choisir le codec audio
      recorder.setFormat("webm"); // Format de sortie

      // Améliorer la qualité vidéo
      recorder.setFrameRate(grabber.getFrameRate());
      recorder.setVideoBitrate(2000 * 1000); // Bitrate vidéo : 2000 kbps
      recorder.setVideoOption("crf", "10"); // CRF pour ajuster la qualité

      // Améliorer la qualité audio
      recorder.setAudioBitrate(128 * 1000); // Bitrate audio : 128 kbps
      recorder.setSampleRate(grabber.getSampleRate());

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
