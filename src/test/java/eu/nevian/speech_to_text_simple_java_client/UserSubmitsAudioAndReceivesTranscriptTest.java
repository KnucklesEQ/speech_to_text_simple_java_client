package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFileHelper;
import eu.nevian.speech_to_text_simple_java_client.exceptions.LoadingConfigurationException;
import eu.nevian.speech_to_text_simple_java_client.utils.ConfigLoader;
import eu.nevian.speech_to_text_simple_java_client.utils.MessageManager;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class UserSubmitsAudioAndReceivesTranscriptTest {
    private static final String AUDIO_FILE_MP3_PATH = "src/test/resources/sample_audio.mp3";
    private static final String API_KEY_FILE_PATH = "config.properties";

    @Test
    public void testUserSubmitsCorrectPathToAudioFile() {
        String expectedMessage = MessageManager.getFileFoundMessage(AUDIO_FILE_MP3_PATH);
        try {
            String actualMessage = AudioFileHelper.validateFile(AUDIO_FILE_MP3_PATH);
            assertEquals(expectedMessage, actualMessage);
        } catch (FileNotFoundException e) {
            fail("FileNotFound exception should not be thrown");
        }
    }

    @Test
    public void testUserSubmitsCorrectAudioFile() {
        assertEquals("audio", AudioFileHelper.GetFileType(AUDIO_FILE_MP3_PATH));
    }

    @Test
    public void testSystemExtractsAPIKeyFromConfigFile() {
        try{
            String apiKey = ConfigLoader.getApiKey(API_KEY_FILE_PATH);
            assertNotNull(apiKey, "API Key should not be null");
            assertFalse(apiKey.isEmpty(), "API Key should not be empty");
        } catch (IOException | LoadingConfigurationException e) {
            fail("Exception should not be thrown");
        }
    }
}
