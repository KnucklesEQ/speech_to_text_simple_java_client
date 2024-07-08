package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFile;
import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFileHelper;
import eu.nevian.speech_to_text_simple_java_client.commandlinemanagement.CommandLineManagement;
import eu.nevian.speech_to_text_simple_java_client.commandlinemanagement.CommandLineOptions;
import eu.nevian.speech_to_text_simple_java_client.exceptions.AudioFileValidationException;
import eu.nevian.speech_to_text_simple_java_client.exceptions.LoadingConfigurationException;
import eu.nevian.speech_to_text_simple_java_client.transcriptionservice.ApiService;
import eu.nevian.speech_to_text_simple_java_client.transcriptionservice.WhisperApiService;
import eu.nevian.speech_to_text_simple_java_client.utils.ConfigLoader;
import eu.nevian.speech_to_text_simple_java_client.utils.TextFileHelper;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final String API_KEY_FILE_PATH = "config.properties";
    private static final int MAX_FILE_SIZE_IN_BYTES = 20 * 1024 * 1024; // 20 MB

    public static void main(String[] args) {
        // Step 1: Parse command line arguments
        CommandLineManagement commandLineManagement = new CommandLineManagement();
        CommandLineOptions cmdOptions = null;

        try {
            cmdOptions = commandLineManagement.parseCommandLineArguments(args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments. " + e.getMessage());
            System.exit(1);
        }

        if (cmdOptions.hasHelpOption()) {
            cmdOptions.printCustomHelp();
            System.exit(0);
        }

        String version = cmdOptions.getVersionOption();
        if (version != null) {
            System.out.println("Speech to Text Simple Java Client version " + version);
            System.exit(0);
        }

        String language = cmdOptions.getLanguageOption();
        if (language != null) {
            if (language.length() != 2 || languageIsNotSupported(language)) {
                System.err.println("Error: Invalid language code");
                cmdOptions.printCustomHelp();
                System.exit(1);
            }
        } else {
            language = "en"; // default language
        }

        List<String> positionalArgs = cmdOptions.getRemainingArgs();

        if (positionalArgs.isEmpty()) {
            System.err.println("Error: Missing required file path argument");
            cmdOptions.printCustomHelp();
            System.exit(1);
        }

        System.out.println("Welcome!\n");

        // Step 2: Load API key from file (config.properties)
        final String apiKey;

        try {
            apiKey = ConfigLoader.getApiKey(API_KEY_FILE_PATH);
        } catch (LoadingConfigurationException | IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        AudioFile audioFile = new AudioFile();
        audioFile.setFilePath(positionalArgs.get(0));

        // Step 3: Check if the file exists and type
        try {
            System.out.println("Validating file...\n");
            System.out.println(AudioFileHelper.validateFile(audioFile.getFilePath()));

            String fileType = AudioFileHelper.GetFileType(audioFile.getFilePath());
            audioFile.setFileType(fileType);
        } catch (AudioFileValidationException | FileNotFoundException e) {
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
            audioFile.setFileSize(AudioFileHelper.getAudioFileSize(audioFile.getFilePath()));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // Step 6: Print the info about the audio file that we are working with
        System.out.println(audioFile);

        // Step 7: Split the audio file if it is too big (max size admitted by OpenAI API is 25 MB)
        final List<AudioFile> audioFileList = new ArrayList<>();

        try {
            if (audioFile.getFileSize() > MAX_FILE_SIZE_IN_BYTES) {
                System.out.println("\nFile is too big. Splitting it into smaller files...\n");
            }

            audioFileList.addAll(AudioFileHelper.splitAudioFileBySize(audioFile, MAX_FILE_SIZE_IN_BYTES));

            // Step 7b: Print the info of the audio files split from the original one
            if (audioFileList.size() > 1) {
                System.out.println("Audio split into " + audioFileList.size() + " smaller files:");
                for (AudioFile af : audioFileList) {
                    System.out.println(af.toString());
                }
            }
        } catch (IOException e) {
            System.err.println("Error splitting audio file: " + e.getMessage());
            System.exit(1);
        }

        // Step 8: It's time to call the API
        ApiService apiService = new WhisperApiService();

        try {
            System.out.println("\n###### Checking access to OpenAI API: Whisper model ######");
            String responseText = apiService.checkAiModelIsAvailable(apiKey);
            System.out.println("\nAPI Response: " + (!responseText.isEmpty()));

            System.out.println("\n###### Transcribe audio to text ######");

            StringBuilder audioTranscription = new StringBuilder();

            for (AudioFile af : audioFileList) {
                // This will append the separator only if there's already content in the audioTranscription.
                if (audioTranscription.length() > 0) {
                    audioTranscription.append("\n//\n");
                }
                audioTranscription.append(apiService.transcribeAudioFile(apiKey, language, af.getFilePath()));
            }

            TextFileHelper.saveTranscriptionToFile(audioTranscription.toString(), "transcription.txt");
            System.out.println("\n\nDONE!\n");

            System.out.println("The API response has: " + audioTranscription.length() + " characters and "
                    + TextFileHelper.countWords(audioTranscription.toString()) + " words.\n");
        } catch (IOException e) {
            System.err.println("Error fetching data from API: " + e.getMessage());
            System.exit(1);
        }

        //Step 9: Ask the user if he wants to move the transcription file to the same folder as the audio file
        System.out.print("Do you want to move the transcription.txt file to the same folder as the audio file? (y/n) ");
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

                System.out.println("Transcription file has been moved to the audio file's folder.");
                System.out.println("You can find it at: " + new File(destinationFolderPath, destinationFileName).getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to move the transcription file.");
                System.err.println("You can find it at: " + new File("transcription.txt").getAbsolutePath());
            }
        } else {
            System.out.println("Transcription file will not be moved. You can find it at: " + new File("transcription.txt").getAbsolutePath());
        }
    }

    private static boolean languageIsNotSupported(String language) {
        Set<String> supportedLanguages = new HashSet<>(Arrays.asList(
                "af", "ar", "hy", "az", "be", "bs", "bg", "ca", "zh", "hr", "cs", "da", "nl", "en", "et", "fi", "fr", "gl", "de", "el", "he", "hi", "hu", "is", "id", "it", "ja", "kn", "kk", "ko", "lv", "lt", "mk", "ms", "mr", "mi", "ne", "no", "fa", "pl", "pt", "ro", "ru", "sr", "sk", "sl", "es", "sw", "sv", "tl", "ta", "th", "tr", "uk", "ur", "vi", "cy"
        ));

        return !supportedLanguages.contains(language);
    }
}
