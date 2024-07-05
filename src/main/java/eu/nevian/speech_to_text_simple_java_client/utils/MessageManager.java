package eu.nevian.speech_to_text_simple_java_client.utils;

public class MessageManager {
    private static final String FILE_FOUND = "File found at: %s.";
    private static final String FILE_NOT_FOUND = "Error: File not found: %s.";
    private static final String API_KEY_NOT_FOUND = "Error: API key not found in config file.";
    private static final String CONFIG_FILE_NOT_FOUND = "Error: Unable to find config.properties file.";
    private static final String CONFIG_FILE_READ_ERROR = "Error reading config.properties file: %s.";

    public static String getFileFoundMessage(String filePath) {
        return String.format(FILE_FOUND, filePath);
    }

    public static String getFileNotFoundMessage(String filePath) {
        return String.format(FILE_NOT_FOUND, filePath);
    }

    public static String getApiKeyNotFoundMessage() {
        return API_KEY_NOT_FOUND;
    }

    public static String getConfigFileNotFoundMessage() {
        return CONFIG_FILE_NOT_FOUND;
    }

    public static String getConfigFileReadErrorMessage(String errorMessage) {
        return String.format(CONFIG_FILE_READ_ERROR, errorMessage);
    }
}
