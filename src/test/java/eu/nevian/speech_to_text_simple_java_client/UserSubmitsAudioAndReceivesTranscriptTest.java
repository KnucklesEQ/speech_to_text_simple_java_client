package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFileHelper;
import eu.nevian.speech_to_text_simple_java_client.exceptions.LoadingConfigurationException;
import eu.nevian.speech_to_text_simple_java_client.transcriptionservice.WhisperApiService;
import eu.nevian.speech_to_text_simple_java_client.utils.ConfigLoader;
import eu.nevian.speech_to_text_simple_java_client.utils.MessageManager;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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


    @Test
    public void testWithValidApiKeyCheckIfAiServiceIsAvailable() throws IOException {
        String validApiKey = "valid-api-key";

        // Create a mock OkHttpClient and a mock Call
        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);

        // Capture the Request object
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        // Mock the response
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("https://some-url").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200) // simulate HTTP 200
                .message("OK")
                .body(ResponseBody.Companion.create(
                        "{\"id\": \"whisper-1\"}",
                        MediaType.get("application/json; charset=utf-8"))
                )
                .build();

        when(mockCall.execute()).thenReturn(mockResponse);

        WhisperApiService service = new WhisperApiService(mockClient);
        String result = service.checkAiModelIsAvailable(validApiKey);

        // Verify the Request was created and capture it
        verify(mockClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        assertFalse(capturedRequest.url().toString().isEmpty());
        assertEquals("Bearer valid-api-key", capturedRequest.header("Authorization"));
        assertNotNull(capturedRequest.header("OpenAI-Organization"));

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
