package eu.nevian.speech_to_text_simple_java_client.audiofile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nevian.speech_to_text_simple_java_client.exceptions.AudioFileValidationException;
import eu.nevian.speech_to_text_simple_java_client.utils.FfmpegProcessHelper;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for audio files.
 */
public class AudioFileHelper {
    /**
     * Private constructor to prevent instantiation. All methods are static.
     */
    private AudioFileHelper() {
    }

    public static String validateFileAndGetType(String filePath) throws AudioFileValidationException {
        final Path path = Paths.get(filePath);

        // Check if the file exists
        if (!Files.exists(path)) {
            throw new AudioFileValidationException("File not found: " + filePath);
        }

        System.out.println("File found at: " + filePath);

        // Check the file type
        final Tika tika = new Tika();

        MediaType mediaType = MediaType.parse(tika.detect(filePath));
        String fileType = mediaType.getType();

        if (!fileType.equals("audio") && !fileType.equals("video")) {
            throw new AudioFileValidationException("Invalid file type. Please provide an audio or video file.");
        }

        System.out.println("\nFile type validated: " + fileType + " file.");

        return fileType;
    }

    /**
     * Extract the audio from a video file. The audio extracted is saved in the same directory as the video file in a
     * file with the same name as the video file but with the .mp3 extension.
     *
     * @param videoFilePath The path to the video file
     * @return The path to the extracted audio file
     * @throws IOException If ffmpeg is not available on the system or if the process was interrupted
     */
    public static String extractAudioFromVideo(String videoFilePath) throws IOException {
        if (FfmpegProcessHelper.isFfmpegNotAvailable()) {
            throw new IOException("ffmpeg is not available on this system. You can install it with 'sudo apt install " +
                    "ffmpeg' on your Linux distribution.");
        }

        System.out.println("Extracting audio from video file...");

        String audioFilePath = videoFilePath.replaceFirst("[.][^.]+$", "") + ".mp3";

        ProcessBuilder processBuilder = FfmpegProcessHelper.createExtractAudioProcessBuilder(videoFilePath, audioFilePath);
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

    /**
     * Get the duration of an audio file in seconds. This method uses the ffprobe command, which is a part of the ffmpeg
     * suite.
     *
     * @param audioFilePath The path to the audio file.
     * @return The duration of the audio file in seconds.
     * @throws IOException If an error occurs while getting the duration of the audio file.
     */
    public static double getAudioFileDuration(String audioFilePath) throws IOException {
        if (FfmpegProcessHelper.isFfmpegNotAvailable()) {
            throw new IOException("ffmpeg is not available on this system. You can install it with 'sudo apt install ffmpeg' on your Linux distribution.");
        }

        ProcessBuilder processBuilder = FfmpegProcessHelper.createGetAudioDurationProcessBuilder(audioFilePath);
        Process process = processBuilder.start();

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Error getting video duration: ffprobe exit code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Error getting video duration: ffprobe process was interrupted", e);
        }

        // Read the output of the ffprobe command
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(process.getInputStream());
        JsonNode durationNode = rootNode.path("format").path("duration");

        if (durationNode.isMissingNode()) {
            throw new IOException("Error getting video duration: Duration not found in ffprobe output");
        }

        return durationNode.asDouble();
    }


    /**
     * Get the file size of a file in bytes.
     *
     * @param filePath The path to the file.
     * @return The file size in bytes.
     * @throws IOException If an error occurs while getting the file size.
     */
    public static long getAudioFileSize(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.size(path);
    }

    /**
     * Split an audio file into multiple parts, each one with a maximum size of maxSizeInBytes. This method uses the
     * ffmpeg command.
     *
     * @param audioFile      The audio file to split.
     * @param maxSizeInBytes The maximum size of each part in bytes.
     * @return A list of audio files, each one with a maximum size of maxSizeInBytes.
     * @throws IOException If an error occurs while splitting the audio file.
     */
    public static List<AudioFile> splitAudioFileBySize(AudioFile audioFile, long maxSizeInBytes) throws IOException {
        List<AudioFile> splitFiles = new ArrayList<>();

        if (audioFile.getFileSize() <= maxSizeInBytes) {
            splitFiles.add(audioFile);
            return splitFiles;
        }

        // Calculate the number of parts needed
        int numberOfParts = (int) Math.ceil((double) audioFile.getFileSize() / maxSizeInBytes);

        // Calculate the duration for each part
        double partDuration = audioFile.getDuration() / numberOfParts;

        // Split the audio file into parts
        for (int i = 0; i < numberOfParts; i++) {
            double startTime = i * partDuration;
            String outputFilePath = audioFile.getFilePath().replaceFirst("[.][^.]+$", "") + "-part" + (i + 1) + ".mp3";

            ProcessBuilder processBuilder = FfmpegProcessHelper.createCutAudioProcessBuilder(
                    audioFile.getFilePath(), outputFilePath, startTime, partDuration);
            Process process = processBuilder.start();

            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("Error splitting audio file: ffmpeg exit code " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Error splitting audio file: ffmpeg process was interrupted", e);
            }

            // Create a new AudioFile object for the part
            AudioFile splitAudioFile = new AudioFile();
            splitAudioFile.setFilePath(outputFilePath);
            splitAudioFile.setFileType("audio");
            splitAudioFile.setDuration(partDuration);
            splitAudioFile.setFileSize(Files.size(Paths.get(outputFilePath)));

            splitFiles.add(splitAudioFile);
        }

        return splitFiles;
    }
}
