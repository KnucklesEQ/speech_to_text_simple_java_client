package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFileHelper;
import eu.nevian.speech_to_text_simple_java_client.utils.MessageManager;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class UserSubmitsAudioAndReceivesTranscriptTest {

    @Test
    public void UserSubmitsCorrectPathToAudioFile() {
        String audioFilePath = "src/test/resources/sample_audio.mp3";
        String expectedMessage = MessageManager.getFileFoundMessage(audioFilePath);
        try {
            String actualMessage = AudioFileHelper.validateFile(audioFilePath);
            assertEquals(expectedMessage, actualMessage);
        } catch (FileNotFoundException e) {
            fail("FileNotFound exception should not be thrown");
        }
    }

    @Test
    public void UserSubmitsCorrectAudioFile() {
        String audioFilePath = "src/test/resources/sample_audio.mp3";
        assertEquals("audio", AudioFileHelper.GetFileType(audioFilePath), "El archivo de audio debe ser de tipo audio");
    }
}
