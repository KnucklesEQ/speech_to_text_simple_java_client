package eu.nevian.speech_to_text_simple_java_client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TextFileHelper {
    private TextFileHelper() {
    }

    public static void saveTranscriptionToFile(String transcription, String outputFilePath) throws IOException {
        Files.writeString(Path.of(outputFilePath), transcription);
    }

    public static void moveTranscriptionFile(File sourceFile, String destinationFolder, String destinationFileName) throws IOException {
        File destinationFile = new File(destinationFolder, destinationFileName);

        Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
