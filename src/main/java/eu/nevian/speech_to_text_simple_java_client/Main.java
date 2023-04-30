package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFile;
import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFileHelper;
import eu.nevian.speech_to_text_simple_java_client.exceptions.AudioFileValidationException;
import eu.nevian.speech_to_text_simple_java_client.transcriptionservice.ApiService;
import eu.nevian.speech_to_text_simple_java_client.utils.TextFileHelper;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final String API_KEY_FILE_PATH = "config.properties";
    private static final int MAX_FILE_SIZE_IN_BYTES = 24 * 1024 * 1024; // 24 MB

    public static void main(String[] args) {
        Options options = new Options();

        Option helpOption = new Option("h", "help", false, "Show help");
        helpOption.setArgName(" ");
        options.addOption(helpOption);

        Option versionOption = new Option("v", "version", false, "Show version");
        versionOption.setArgName(" ");
        options.addOption(versionOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                printCustomHelp(options);
                System.exit(0);
            }

            if (cmd.hasOption("version")) {
                System.out.println("Speech to Text Simple Java Client version " + getVersion());
                System.exit(0);
            }
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            System.exit(1);
        }

        // Get the remaining positional arguments
        List<String> positionalArgs = cmd.getArgList();

        // Check if the file path argument is provided
        if (positionalArgs.isEmpty()) {
            System.err.println("Error: Missing required file path argument");
            printCustomHelp(options);
            System.exit(1);
        }

        System.out.println("Welcome!\n");

        // Step 1: Load API key from file (config.properties)
        final String apiKey = loadApiKey();

        AudioFile audioFile = new AudioFile();
        audioFile.setFilePath(positionalArgs.get(0));

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
        ApiService apiService = new ApiService();

        try {
            System.out.println("\n###### Checking access to OpenAI API: Whisper model ######");
            String responseText = apiService.checkWhisperOpenAiModel(apiKey);
            System.out.println("\nAPI Response: " + (!responseText.isEmpty()));

            System.out.println("\n###### Transcribe audio to text ######");

            StringBuilder audioTranscription = new StringBuilder();

            for (AudioFile af : audioFileList) {
                // This will append the separator only if there's already content in the audioTranscription.
                if (audioTranscription.length() > 0) {
                    audioTranscription.append("\n//\n");
                }
                audioTranscription.append(apiService.transcribeAudioFile(apiKey, af.getFilePath()));
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

    private static String loadApiKey() {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(API_KEY_FILE_PATH)) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find config.properties file.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error reading config.properties file: " + e.getMessage());
            System.exit(1);
        }

        String apiKey = properties.getProperty("api_key");
        if (apiKey == null) {
            System.err.println("API key not found in config.properties file.");
            System.exit(1);
        }

        return apiKey;
    }

    public static String getVersion() {
        String version = Main.class.getPackage().getImplementationVersion();
        return version != null ? version : "unknown";
    }

    private static void printCustomHelp(Options options) {
        System.out.println("Usage:");
        System.out.println("  java -jar speech_to_text_simple_java_client.jar [options] <FILE>");
        System.out.println("\nOptions:");
        System.out.printf("  %-2s %-8s  %s%n", "<FILE>", "", "Path to audio file or video file to transcribe");
        for (Option option : options.getOptions()) {
            System.out.printf("  -%s,--%-10s  %s%n", option.getOpt(), option.getLongOpt(), option.getDescription());
        }
    }
}
