package eu.nevian.speech_to_text_simple_java_client.utils;

public class MessageManager {
    private static final String FILE_FOUND = "File found at: %s.";
    private static final String FILE_NOT_FOUND = "Error: File not found: %s.";
    private static final String FILE_NOT_REGULAR = "Error: Path is not a regular file: %s.";
    private static final String FILE_NOT_READABLE = "Error: File is not readable: %s.";
    private static final String FILE_TYPE_VALIDATED = "File type validated: %s file.";
    private static final String API_KEY_NOT_FOUND = "Error: API key not found in config file.";
    private static final String CONFIG_FILE_NOT_FOUND = "Error: Unable to find config.properties file at: %s.";
    private static final String CONFIG_FILE_READ_ERROR = "Error reading config.properties file: %s.";
    private static final String CONFIG_FILE_NOT_FOUND_GUIDANCE = """
            Error: config.properties file not found.
            Expected location: %s
            This file is required to run the application.

            Minimal example:
            api_key=YOUR_OPENAI_API_KEY
            audio_file_limit_size_in_bytes=25000000
            language=en
            """;

    public static String getFileFoundMessage(String filePath) {
        return String.format(FILE_FOUND, filePath);
    }

    public static String getFileNotFoundMessage(String filePath) {
        return String.format(FILE_NOT_FOUND, filePath);
    }

    public static String getFileNotRegularMessage(String filePath) {
        return String.format(FILE_NOT_REGULAR, filePath);
    }

    public static String getFileNotReadableMessage(String filePath) {
        return String.format(FILE_NOT_READABLE, filePath);
    }

    public static String getFileTypeValidatedMessage(String fileType) {
        return String.format(FILE_TYPE_VALIDATED, fileType);
    }

    public static String getApiKeyNotFoundMessage() {
        return API_KEY_NOT_FOUND;
    }

    public static String getConfigFileNotFoundMessage(String configFilePath) {
        return String.format(CONFIG_FILE_NOT_FOUND, configFilePath);
    }

    public static String getConfigFileReadErrorMessage(String errorMessage) {
        return String.format(CONFIG_FILE_READ_ERROR, errorMessage);
    }

    public static String getConfigFileNotFoundGuidanceMessage(String configFilePath) {
        return String.format(CONFIG_FILE_NOT_FOUND_GUIDANCE, configFilePath);
    }
}
