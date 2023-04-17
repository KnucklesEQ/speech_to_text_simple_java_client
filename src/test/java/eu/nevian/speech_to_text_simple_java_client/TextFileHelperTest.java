package eu.nevian.speech_to_text_simple_java_client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextFileHelperTest {
    @Test
    public void testSaveTranscriptionToFile() throws IOException {
        // Create sample transcription text
        String transcription = "This is a test transcription.";

        // Create a temporary output file path
        Path outputFilePath = Files.createTempFile("transcription_test", ".txt");

        // Call saveTranscriptionToFile method
        TextFileHelper.saveTranscriptionToFile(transcription, outputFilePath.toString());

        // Read the content from the output file
        String fileContent = Files.readString(outputFilePath);

        // Compare the content with the original transcription
        assertEquals(transcription, fileContent);

        // Clean up the temporary file
        Files.deleteIfExists(outputFilePath);
    }
}
