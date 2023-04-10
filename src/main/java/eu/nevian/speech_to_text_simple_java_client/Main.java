package eu.nevian.speech_to_text_simple_java_client;

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome!");

        if (args.length < 1) {
            System.out.println("Usage: java -jar speech_to_text_simple_java_client.jar <audio_file_path>");
            System.exit(1);
        }

        String filePath = args[0];
        String fileType = validateFile(filePath);
        String apiKey = loadApiKey();

        if (fileType.equals("video")) {
            try {
                filePath = extractAudioFromVideo(filePath);
            } catch (IOException e) {
                System.err.println("Error extracting audio from video: " + e.getMessage());
                System.exit(1);
            }
        }

        ApiService apiService = new ApiService();

        try {
            System.out.println();
            System.out.println("###### Trying access to OpenAI API ######");
            String responseText = apiService.checkWhisperOpenAiModel(apiKey);
            System.out.println("API Response: ");
            System.out.println(responseText);

            System.out.println();
            System.out.println("###### Transcribe audio to text ######");
            //String audioTranscription = apiService.transcribeAudioFile(apiKey, filePath);
            System.out.println("Text: ");
            //System.out.println(audioTranscription);
        } catch (IOException e) {
            System.err.println("Error fetching data from API: " + e.getMessage());
        }
    }

    private static String validateFile(String filePath) {
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

    private static String loadApiKey() {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream("config.properties")) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find config.properties file.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error reading config.properties file.");
            e.printStackTrace();
            System.exit(1);
        }

        String apiKey = properties.getProperty("api_key");
        if (apiKey == null) {
            System.err.println("API key not found in config.properties file.");
            System.exit(1);
        }

        return apiKey;
    }

    private static String extractAudioFromVideo(String videoFilePath) throws IOException {
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
