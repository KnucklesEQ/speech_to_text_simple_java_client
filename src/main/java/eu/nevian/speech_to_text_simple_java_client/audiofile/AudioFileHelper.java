package eu.nevian.speech_to_text_simple_java_client.audiofile;

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AudioFileHelper {
    private AudioFileHelper() {}

    public static String validateFile(String filePath) {
        final Path path = Paths.get(filePath);

        // Check if the file exists
        if (!Files.exists(path)) {
            System.err.println("File not found: " + filePath);
            System.exit(1);
        }

        // Check the file type
        final Tika tika = new Tika();

        MediaType mediaType = MediaType.parse(tika.detect(filePath));
        String fileType = mediaType.getType();

        if (!fileType.equals("audio") && !fileType.equals("video")) {
            System.err.println("Invalid file type. Please provide an audio or video file.");
            System.exit(1);
        }

        System.out.println("File type: " + fileType);

        return fileType;
    }

    public static String extractAudioFromVideo(String videoFilePath) throws IOException {
        String audioFilePath = videoFilePath.replaceFirst("[.][^.]+$", "") + ".mp3";

        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", videoFilePath, "-vn", "-acodec", "libmp3lame", audioFilePath);
        Process process = processBuilder.start();

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Error extracting audio from video: ffmpeg exit code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Error extracting audio from video: ffmpeg process was interrupted", e);
        }

        return audioFilePath;
    }
}
