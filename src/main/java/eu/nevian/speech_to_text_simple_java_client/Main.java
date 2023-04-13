package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFile;
import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFileHelper;
import eu.nevian.speech_to_text_simple_java_client.exceptions.AudioFileValidationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome!");

        final String apiKey = loadApiKey();

        if (args.length < 1) {
            System.out.println("Usage: java -jar speech_to_text_simple_java_client.jar <audio_file_path>");
            System.exit(1);
        }

        AudioFile audioFile = new AudioFile();
        audioFile.setFilePath(args[0]);

        String fileType;

        try {
            fileType = AudioFileHelper.validateFile(audioFile.getFilePath());
            audioFile.setFileType(fileType);
        } catch (AudioFileValidationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (audioFile.getFileType().equals("video")) {
            String osName = System.getProperty("os.name").toLowerCase();

            if(osName.contains("linux")) {
                try {
                    String audioFilePath = AudioFileHelper.extractAudioFromVideo(audioFile.getFilePath());
                    audioFile.setFilePath(audioFilePath);
                    audioFile.setFileType("audio");
                } catch (IOException e) {
                    System.err.println("Error extracting audio from video: " + e.getMessage());
                    System.exit(1);
                }
            } else {
                System.err.println("Error: Video file processing is supported only on Linux.");
                System.exit(1);
            }
        }

        try {
            double audioDuration = AudioFileHelper.getAudioFileDuration(audioFile.getFilePath());
            audioFile.setDuration(audioDuration);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        ApiService apiService = new ApiService();

        try {
            System.out.println();
            System.out.println("###### Trying access to OpenAI API ######");
            String responseText = apiService.checkWhisperOpenAiModel(apiKey);
            System.out.println();
            System.out.println("API Response: ");
            System.out.println(responseText);

            System.out.println();
            System.out.println("###### Transcribe audio to text ######");
            String audioTranscription = apiService.transcribeAudioFile(apiKey, audioFile.getFilePath());
            TextFileHelper.saveTranscriptionToFile(audioTranscription, "transcription.txt");
            System.out.println("DONE!");
        } catch (IOException e) {
            System.err.println("Error fetching data from API: " + e.getMessage());
        }
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
}
