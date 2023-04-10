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
            System.out.println("Usage: java -jar <audio_file_path>");
            System.exit(1);
        }

        String audioFilePath = args[0];

        Path filePath = Paths.get(audioFilePath);

        // Check if the file exists
        if (!Files.exists(filePath)) {
            System.err.println("File not found: " + audioFilePath);
            System.exit(1);
        }

        // Check the file type
        Tika tika = new Tika();
        String fileType = "";

        try {
            String mimeType = tika.detect(filePath);
            MediaType mediaType = MediaType.parse(mimeType);
            fileType = mediaType.getType();
        } catch (IOException e) {
            System.err.println("Error detecting file type: " + e.getMessage());
            System.exit(1);
        }

        if (!fileType.equals("audio") && !fileType.equals("video")) {
            System.err.println("Invalid file type. Please provide an audio or video file.");
            System.exit(1);
        }

        System.out.println("File type: " + fileType);

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

        ApiService apiService = new ApiService();

        try {
            System.out.println();
            System.out.println("###### Trying access to OpenAI API ######");
            String responseText = apiService.checkWhisperOpenAiModel(apiKey);
            System.out.println("API Response: ");
            System.out.println(responseText);

            System.out.println();
            System.out.println("###### Transcribe audio to text ######");
            //String audioTranscription = apiService.transcribeAudioFile(apiKey, audioFilePath);
            System.out.println("Text: ");
            //System.out.println(audioTranscription);
        } catch (IOException e) {
            System.err.println("Error fetching data from API: " + e.getMessage());
        }
    }
}
