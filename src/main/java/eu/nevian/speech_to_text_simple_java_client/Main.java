package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFile;
import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFileHelper;
import eu.nevian.speech_to_text_simple_java_client.commandlinemanagement.CommandLineManagement;
import eu.nevian.speech_to_text_simple_java_client.commandlinemanagement.CommandLineOptions;
import eu.nevian.speech_to_text_simple_java_client.exceptions.FileValidationException;
import eu.nevian.speech_to_text_simple_java_client.exceptions.LoadingConfigurationException;
import eu.nevian.speech_to_text_simple_java_client.transcriptionservice.ApiService;
import eu.nevian.speech_to_text_simple_java_client.transcriptionservice.WhisperApiService;
import eu.nevian.speech_to_text_simple_java_client.utils.ConfigLoader;
import eu.nevian.speech_to_text_simple_java_client.utils.FileType;
import eu.nevian.speech_to_text_simple_java_client.utils.FfmpegProcessHelper;
import eu.nevian.speech_to_text_simple_java_client.utils.LanguageSupport;
import eu.nevian.speech_to_text_simple_java_client.utils.MessageManager;
import eu.nevian.speech_to_text_simple_java_client.utils.TextFileHelper;
import eu.nevian.speech_to_text_simple_java_client.utils.TemporaryWorkspaceHelper;
import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Main {
    private static final String CONFIG_FILE_PATH = "config.properties";

    public static void main(String[] args) {
        System.exit(run(args));
    }

    private static int run(String[] args) {
        // Step 1: Parse command line arguments
        CommandLineManagement commandLineManagement = new CommandLineManagement();
        CommandLineOptions cmdOptions = null;

        try {
            cmdOptions = commandLineManagement.parseCommandLineArguments(args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments. " + e.getMessage());
            return 1;
        }

        if (cmdOptions.hasHelpOption()) {
            cmdOptions.printCustomHelp();
            return 0;
        }

        String version = cmdOptions.getVersionOption();
        if (version != null) {
            System.out.println("Speech to Text Simple Java Client version " + version);
            return 0;
        }

        List<String> positionalArgs = cmdOptions.getRemainingArgs();

        if (positionalArgs.isEmpty()) {
            System.err.println("Error: Missing required file path argument");
            cmdOptions.printCustomHelp();
            return 1;
        }

        if (positionalArgs.size() > 1) {
            System.err.println("Error: Too many file path arguments. Expected exactly one <FILE>.");
            cmdOptions.printCustomHelp();
            return 1;
        }

        // Keep the original input path.
        String originalInputPath = positionalArgs.get(0);
        Path originalInputResolvedPath = Path.of(originalInputPath).toAbsolutePath().normalize();
        Path originalInputDirectoryPath = originalInputResolvedPath.getParent() != null
                ? originalInputResolvedPath.getParent()
                : Path.of(".").toAbsolutePath().normalize();
        String originalInputFileName = originalInputResolvedPath.getFileName() != null
                ? originalInputResolvedPath.getFileName().toString()
                : originalInputResolvedPath.toString();
        int originalInputExtensionSeparatorIndex = originalInputFileName.lastIndexOf('.');
        String originalInputBaseName = originalInputExtensionSeparatorIndex > 0
                ? originalInputFileName.substring(0, originalInputExtensionSeparatorIndex)
                : originalInputFileName;
        // Resolve language in order: CLI option -> config.properties -> LanguageSupport.DEFAULT_LANGUAGE.
        String language = null;
        String languageOption = cmdOptions.getLanguageOption();
        if (languageOption != null) {
            language = languageOption.trim().toLowerCase(Locale.ROOT);
            if (language.length() != 2 || LanguageSupport.isNotSupported(language)) {
                System.err.println("Error: Invalid language code");
                cmdOptions.printCustomHelp();
                return 1;
            }

            try {
                // Persist the CLI language for future runs.
                ConfigLoader.saveLanguage(CONFIG_FILE_PATH, language);
            } catch (IOException e) {
                System.err.println("Warning: Failed to save language to config.properties: " + e.getMessage());
            }
        } else {
            try {
                language = ConfigLoader.getLanguage(CONFIG_FILE_PATH);
            } catch (IOException e) {
                System.err.println("Warning: Failed to read language from config.properties: " + e.getMessage());
            }

            if (language != null) {
                String rawLanguage = language;
                language = language.trim().toLowerCase(Locale.ROOT);
                if (language.length() != 2 || LanguageSupport.isNotSupported(language)) {
                    System.err.println("Warning: Invalid language in config.properties (" + rawLanguage
                            + "). Falling back to \"" + LanguageSupport.DEFAULT_LANGUAGE + "\".");
                    language = LanguageSupport.DEFAULT_LANGUAGE;
                }
            } else {
                language = LanguageSupport.DEFAULT_LANGUAGE;
            }
        }

        if (FfmpegProcessHelper.isFfmpegNotAvailable()) {
            System.err.println("Error: ffmpeg is not available on this system. Please install it and ensure it is available on PATH.");
            return 1;
        }

        if (FfmpegProcessHelper.isFfprobeNotAvailable()) {
            System.err.println("Error: ffprobe is not available on this system. Please install it and ensure it is available on PATH.");
            return 1;
        }

        System.out.println("Welcome!\n");

        AudioFile audioFile = new AudioFile();
        List<AudioFile> audioFileList = new ArrayList<>();

        // Step 2: Check if the file exists and type
        try {
            System.out.println("Validating file...\n");

            if (AudioFileHelper.validateFile(originalInputPath)) {
                audioFile.setFilePath(originalInputPath);
                System.out.println(MessageManager.getFileFoundMessage(audioFile.getFilePath()));
            }

            FileType fileType = AudioFileHelper.getFileType(audioFile.getFilePath());
            audioFile.setFileType(fileType);

            System.out.println();
            System.out.println(MessageManager.getFileTypeValidatedMessage(audioFile.getFileType().getType()));
        } catch (FileValidationException | FileNotFoundException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        try (TemporaryWorkspaceHelper temporaryWorkspace = TemporaryWorkspaceHelper.createTemporaryWorkspace()) {
            Path temporaryWorkspacePath = temporaryWorkspace.getWorkspacePath();

            // Step 3: If the file is a video, extract the audio from it
            if (audioFile.getFileType() == FileType.VIDEO) {
                try {
                    System.out.println("\nVideo detected. Extracting audio...\n");
                    String audioFilePath = AudioFileHelper.extractAudioFromVideo(audioFile.getFilePath(), temporaryWorkspacePath);
                    audioFile.setFilePath(audioFilePath);
                    audioFile.setFileType(FileType.AUDIO);
                    System.out.println("Audio extracted to: " + audioFile.getFilePath());
                } catch (IOException e) {
                    System.err.println("Error extracting audio from video: " + e.getMessage());
                    return 1;
                }
            }

            // Step 4: Get audio file duration and size
            try {
                audioFile.setDuration(AudioFileHelper.getAudioFileDuration(audioFile.getFilePath()));
                audioFile.setFileSize(AudioFileHelper.getAudioFileSizeInBytes(audioFile.getFilePath()));
                System.out.println(audioFile);

                // Step 5: Split the audio file if it is too big
                long maxFileSizeInBytes = ConfigLoader.getMaxFileSizeInBytes();
                if (audioFile.getFileSize() > maxFileSizeInBytes) {
                    System.out.println("\nFile is too big. Splitting it into smaller files...\n");
                }

                audioFileList.addAll(
                        AudioFileHelper.splitAudioFileBySize(audioFile, maxFileSizeInBytes, temporaryWorkspacePath)
                );

                if (audioFileList.size() > 1) {
                    System.out.println("Audio split into " + audioFileList.size() + " smaller files:");
                    for (AudioFile af : audioFileList) {
                        System.out.println(af);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                return 1;
            }

            // Step 8: It's time to call the API
            ApiService apiService = new WhisperApiService();

            try {
                String apiKey = ConfigLoader.getApiKey(CONFIG_FILE_PATH);

                System.out.println("\n###### Checking access to OpenAI API: Whisper model ######");
                String responseText = apiService.checkAiModelIsAvailable(apiKey);
                System.out.println("\nAPI Response: " + (!responseText.isEmpty()));

                System.out.println("\n###### Transcribe audio to text ######");

                StringBuilder audioTranscription = new StringBuilder();
                for (AudioFile af : audioFileList) {
                    if (!audioTranscription.isEmpty()) {
                        audioTranscription.append("\n//\n");
                    }
                    audioTranscription.append(apiService.transcribeAudioFile(apiKey, language, af.getFilePath()));
                }

                String transcriptionText = audioTranscription.toString();

                TextFileHelper.saveTranscriptionToFile(transcriptionText, "transcription.txt");
                System.out.println("\n\nDONE!\n");
                System.out.println("The API response has: " + transcriptionText.length() + " characters and "
                        + TextFileHelper.countWords(transcriptionText) + " words.\n");

                System.out.print("Do you want to move the transcription.txt file to the same folder as the original file? (y/n) ");
                String userAnswer;
                try (Scanner scanner = new Scanner(System.in)) {
                    userAnswer = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                }
                boolean shouldMoveTranscriptionFile = "y".equalsIgnoreCase(userAnswer);

                Path sourceTranscriptionPath = Path.of("transcription.txt").toAbsolutePath().normalize();
                String destinationFileName = originalInputBaseName + "_TRANSCRIPTION.txt";
                Path destinationTranscriptionPath = originalInputDirectoryPath.resolve(destinationFileName)
                        .toAbsolutePath()
                        .normalize();

                if (shouldMoveTranscriptionFile) {
                    try {
                        TextFileHelper.moveTranscriptionFile(sourceTranscriptionPath.toFile(), originalInputDirectoryPath.toString(), destinationFileName);
                        System.out.println("Transcription file has been moved to the original file's folder.");
                        System.out.println("You can find it at: " + destinationTranscriptionPath);
                    } catch (IOException e) {
                        System.err.println("Failed to move the transcription file.");
                        System.err.println("You can find it at: " + sourceTranscriptionPath);
                    }
                } else {
                    System.out.println("Transcription file will not be moved. You can find it at: " + sourceTranscriptionPath);
                }
            } catch (IOException | LoadingConfigurationException e) {
                System.err.println("Error fetching data from API: " + e.getMessage());
                return 1;
            }
        } catch (IOException e) {
            System.err.println("Error creating temporary workspace: " + e.getMessage());
            return 1;
        }

        return 0;
    }

}
