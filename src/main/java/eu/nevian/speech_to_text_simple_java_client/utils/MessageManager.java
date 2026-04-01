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
    private static final String CONFIG_FILE_WRITE_ERROR = "Error writing config.properties file: %s.";
    private static final String APPLICATION_DEFAULTS_NOT_FOUND = "Error: Unable to find internal application defaults resource: %s.";
    private static final String APPLICATION_DEFAULTS_READ_ERROR = "Error reading internal application defaults resource %s: %s.";
    private static final String APPLICATION_DEFAULTS_INVALID = "Error: Invalid internal application defaults resource %s. Problem with property: %s.";
    private static final String CONFIG_FILE_NOT_FOUND_GUIDANCE = """
            Error: config.properties file not found.
            Expected location: %s
            This file is required to run the application.

            Minimal example:
            api_key=YOUR_OPENAI_API_KEY
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

    public static String getConfigFileWriteErrorMessage(String errorMessage) {
        return String.format(CONFIG_FILE_WRITE_ERROR, errorMessage);
    }

    public static String getConfigFileNotFoundGuidanceMessage(String configFilePath) {
        return String.format(CONFIG_FILE_NOT_FOUND_GUIDANCE, configFilePath);
    }

    public static String getApplicationDefaultsNotFoundMessage(String resourceName) {
        return String.format(APPLICATION_DEFAULTS_NOT_FOUND, resourceName);
    }

    public static String getApplicationDefaultsReadErrorMessage(String resourceName, String errorMessage) {
        return String.format(APPLICATION_DEFAULTS_READ_ERROR, resourceName, errorMessage);
    }

    public static String getApplicationDefaultsInvalidMessage(String resourceName, String propertyName) {
        return String.format(APPLICATION_DEFAULTS_INVALID, resourceName, propertyName);
    }
}
