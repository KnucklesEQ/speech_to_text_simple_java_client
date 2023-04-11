package eu.nevian.speech_to_text_simple_java_client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextFileHelper {
    private TextFileHelper() {}

    public static void saveTranscriptionToFile(String transcription, String outputFilePath) throws IOException {
        Files.writeString(Path.of(outputFilePath), transcription);
    }
}
