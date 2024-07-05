package eu.nevian.speech_to_text_simple_java_client.utils;

public class MessageManager {
    public static final String FILE_FOUND = "File found at: %s";
    public static final String FILE_NOT_FOUND = "Error: File not found: %s";

    public static String getFileFoundMessage(String filePath) {
        return String.format(FILE_FOUND, filePath);
    }

    public static String getFileNotFoundMessage(String filePath) {
        return String.format(FILE_NOT_FOUND, filePath);
    }
}
