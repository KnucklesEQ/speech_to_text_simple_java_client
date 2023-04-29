package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFile;
import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFileHelper;
import eu.nevian.speech_to_text_simple_java_client.exceptions.AudioFileValidationException;
import eu.nevian.speech_to_text_simple_java_client.utils.TextFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String API_KEY_FILE_PATH = "config.properties";
    private static final int MAX_FILE_SIZE_IN_BYTES = 24 * 1024 * 1024; // 24 MB

    public static void main(String[] args) {
        logger.info("Welcome!\n");

        // Step 1: Load API key from file (config.properties)
        final String apiKey = loadApiKey();

        // Step 2: Check if user provided an audio file path
        if (args.length < 1) {
            logger.error("Usage: java -jar speech_to_text_simple_java_client.jar <audio_file_path>");
            System.exit(1);
        }

        AudioFile audioFile = new AudioFile();
        audioFile.setFilePath(args[0]);

        // Step 3: Check if the file exists and type
        try {
            logger.info("Validating file...\n");
            String fileType = AudioFileHelper.validateFileAndGetType(audioFile.getFilePath());
            audioFile.setFileType(fileType);
        } catch (AudioFileValidationException e) {
            logger.error(e.getMessage());
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
                    logger.error("Error extracting audio from video: " + e.getMessage());
                    System.exit(1);
                }
            } else {
                logger.error("Error: Video file processing is supported only on Linux.");
                System.exit(1);
            }
        }

        // Step 5: Get audio file duration and size
        try {
            audioFile.setDuration(AudioFileHelper.getAudioFileDuration(audioFile.getFilePath()));
            audioFile.setFileSize(AudioFileHelper.getAudioFileSize(audioFile.getFilePath()));
        } catch (IOException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }

        // Step 6: Print the info about the audio file that we are working with
        logger.info(audioFile.toString());

        // Step 7: Split the audio file if it is too big (max size admitted by OpenAI API is 25 MB)
        final List<AudioFile> audioFileList = new ArrayList<>();

        try {
            if (audioFile.getFileSize() > MAX_FILE_SIZE_IN_BYTES) {
                logger.info("\nFile is too big. Splitting it into smaller files...\n");
            }

            audioFileList.addAll(AudioFileHelper.splitAudioFileBySize(audioFile, MAX_FILE_SIZE_IN_BYTES));

            // Step 7b: Print the info of the audio files split from the original one
            if (audioFileList.size() > 1) {
                logger.info("Audio split into " + audioFileList.size() + " smaller files:");
                for (AudioFile af : audioFileList) {
                    logger.info(af.toString());
                }
            }
        } catch (IOException e) {
            logger.error("Error splitting audio file: " + e.getMessage());
            System.exit(1);
        }

        // Step 8: It's time to call the API
        ApiService apiService = new ApiService();

        try {
            logger.info("\n###### Checking access to OpenAI API: Whisper model ######");
            String responseText = apiService.checkWhisperOpenAiModel(apiKey);
            logger.info("\nAPI Response: " + (!responseText.isEmpty()));

            logger.info("\n###### Transcribe audio to text ######");

            StringBuilder audioTranscription = new StringBuilder();

            for (AudioFile af : audioFileList) {
                // This will append the separator only if there's already content in the audioTranscription.
                if (audioTranscription.length() > 0) {
                    audioTranscription.append("\n//\n");
                }
                audioTranscription.append(apiService.transcribeAudioFile(apiKey, af.getFilePath()));
            }

            TextFileHelper.saveTranscriptionToFile(audioTranscription.toString(), "transcription.txt");
            logger.info("\n\nDONE!\n");

            logger.info("The API response has: " + audioTranscription.length() + " characters and "
                    + TextFileHelper.countWords(audioTranscription.toString()) + " words.\n");
        } catch (IOException e) {
            logger.error("Error fetching data from API: " + e.getMessage());
            System.exit(1);
        }

        //Step 9: Ask the user if he wants to move the transcription file to the same folder as the audio file
        logger.info("Do you want to move the transcription.txt file to the same folder as the audio file? (y/n) ");
        String userAnswer;

        try (Scanner scanner = new Scanner(System.in)) {
            userAnswer = scanner.nextLine();
        }

        if (userAnswer.equals("y") || userAnswer.equals("Y")) {
            File sourceFile = new File("transcription.txt");

            String destinationFolderPath = new File(audioFile.getFilePath()).getParent();

            String audioFileName = new File(audioFile.getFilePath()).getName();
            String fileNameWithoutExtension = audioFileName.substring(0, audioFileName.lastIndexOf('.'));
            String destinationFileName = fileNameWithoutExtension + "_TRANSCRIPTION" + ".txt";

            try {
                TextFileHelper.moveTranscriptionFile(sourceFile, destinationFolderPath, destinationFileName);

                logger.info("Transcription file has been moved to the audio file's folder.");
                logger.info("You can find it at: " + new File(destinationFolderPath, destinationFileName).getAbsolutePath());
            } catch (IOException e) {
                logger.error("Failed to move the transcription file.");
                logger.error("You can find it at: " + new File("transcription.txt").getAbsolutePath());
            }
        } else {
            logger.info("Transcription file will not be moved. You can find it at: " + new File("transcription.txt").getAbsolutePath());
        }
    }

    private static String loadApiKey() {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(API_KEY_FILE_PATH)) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            logger.error("Unable to find config.properties file.");
            System.exit(1);
        } catch (IOException e) {
            logger.error("Error reading config.properties file.", e);
            System.exit(1);
        }

        String apiKey = properties.getProperty("api_key");
        if (apiKey == null) {
            logger.error("API key not found in config.properties file.");
            System.exit(1);
        }

        return apiKey;
    }
}
