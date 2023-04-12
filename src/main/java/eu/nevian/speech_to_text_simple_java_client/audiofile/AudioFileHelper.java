package eu.nevian.speech_to_text_simple_java_client.audiofile;

import eu.nevian.speech_to_text_simple_java_client.exceptions.AudioFileValidationException;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AudioFileHelper {
    private AudioFileHelper() {}

    public static String validateFile(String filePath) throws AudioFileValidationException {
        final Path path = Paths.get(filePath);

        // Check if the file exists
        if (!Files.exists(path)) {
            throw new AudioFileValidationException("File not found: " + filePath);
        }

        // Check the file type
        final Tika tika = new Tika();

        MediaType mediaType = MediaType.parse(tika.detect(filePath));
        String fileType = mediaType.getType();

        if (!fileType.equals("audio") && !fileType.equals("video")) {
            throw new AudioFileValidationException("Invalid file type. Please provide an audio or video file.");
        }

        System.out.println("File type validated: " + fileType + " file.");

        return fileType;
    }

    public static String extractAudioFromVideo(String videoFilePath) throws IOException {
        if (!isFfmpegAvailable()) {
            throw new IOException("ffmpeg is not available on this system. You can install it with 'sudo apt install ffmpeg' on your Linux distribution.");
        }

        String audioFilePath = videoFilePath.replaceFirst("[.][^.]+$", "") + ".mp3";

        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", videoFilePath, "-vn", "-acodec", "libmp3lame", "-b:a", "64k", audioFilePath);
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

    private static boolean isFfmpegAvailable() {
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-version");
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
