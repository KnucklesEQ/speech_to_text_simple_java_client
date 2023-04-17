package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFile;
import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFileHelper;
import eu.nevian.speech_to_text_simple_java_client.exceptions.AudioFileValidationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class Main {
    private static final String API_KEY_FILE_PATH = "config.properties";
    private static final int MAX_FILE_SIZE_IN_BYTES = 24 * 1024 * 1024; // 24 MB

    public static void main(String[] args) {
        System.out.println("Welcome!\n");

        // Step 1: Load API key from file (config.properties)
        final String apiKey = loadApiKey();

        // Step 2: Check if user provided an audio file path
        if (args.length < 1) {
            System.out.println("Usage: java -jar speech_to_text_simple_java_client.jar <audio_file_path>");
            System.exit(1);
        }

        AudioFile audioFile = new AudioFile();
        audioFile.setFilePath(args[0]);

        // Step 3: Check if the file exists and type
        try {
            System.out.println("Validating file...\n");
            String fileType = AudioFileHelper.validateFileAndGetType(audioFile.getFilePath());
            audioFile.setFileType(fileType);
        } catch (AudioFileValidationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // Step 4: If the file is a video, extract the audio from it
        if (audioFile.getFileType().equals("video")) {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("linux")) {
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

        // Step 5: Get audio file duration and size
        try {
            audioFile.setDuration(AudioFileHelper.getAudioFileDuration(audioFile.getFilePath()));
            audioFile.setFileSize(AudioFileHelper.getFileSize(audioFile.getFilePath()));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // Step 6: Print the info about the audio file that we are working with
        System.out.println(audioFile);

        // Step 7: Split the audio file if it is too big (max size admitted by OpenAI API is 25 MB)
        try {
            List<AudioFile> aux = AudioFileHelper.splitAudioFileBySize(audioFile, MAX_FILE_SIZE_IN_BYTES);

            // Step 7b: Print the info of the audio files split from the original one
            if (aux.size() > 1) {
                System.out.println();
                System.out.println("Audio file is too big. It will be split into " + aux.size() + " smaller files:");

                for (AudioFile af : aux) {
                    System.out.println(af);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Step 8: It's time to call the API
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

        try (FileInputStream fileInputStream = new FileInputStream(API_KEY_FILE_PATH)) {
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
