package eu.nevian.speech_to_text_simple_java_client.audiofile;

import eu.nevian.speech_to_text_simple_java_client.exceptions.AudioFileValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AudioFileHelperTest {
    @Test
    public void testValidateFile() {
        String filePath = "src/test/resources/sample-audio.mp3";
        String fileType = AudioFileHelper.validateFile(filePath);
        assertEquals("audio", fileType);

        filePath = "src/test/resources/sample-video.mp4";
        fileType = AudioFileHelper.validateFile(filePath);
        assertEquals("video", fileType);

        assertThrows(AudioFileValidationException.class, () -> AudioFileHelper.validateFile("config.properties"));
    }

    @Test
    public void testExtractAudioFromVideo() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("linux")) {
            System.out.println("Skipping testExtractAudioFromVideo on non-Linux system.");
            return;
        }

        String videoFilePath = "src/test/resources/sample-video.mp4";
        String expectedAudioFilePath = videoFilePath.replaceFirst("[.][^.]+$", "") + ".mp3";

        try {
            String audioFilePath = AudioFileHelper.extractAudioFromVideo(videoFilePath);
            assertEquals(expectedAudioFilePath, audioFilePath);

            Path audioPath = Path.of(audioFilePath);
            Files.deleteIfExists(audioPath);
        } catch (IOException e) {
            System.err.println("Error extracting audio from video or deleting audio file: " + e.getMessage());
        }
    }
}

