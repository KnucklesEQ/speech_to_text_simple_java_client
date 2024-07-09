package eu.nevian.speech_to_text_simple_java_client;

import eu.nevian.speech_to_text_simple_java_client.audiofile.AudioFile;
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
public class UserSubmitsAudioMp3FileAndReceivesTranscriptTest {
    private static final String AUDIO_FILE_MP3_PATH = "src/test/resources/sample_audio.mp3";
    private static final String API_KEY_FILE_PATH = "src/test/resources/api.txt";

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
    public void testUserSubmitsCorrectAudioFileType() {
        assertEquals("audio", AudioFileHelper.getFileType(AUDIO_FILE_MP3_PATH));
    }

    @Test
    public void testUserSubmitsAudioFileNotEmpty() {
        try {
            assertTrue(AudioFileHelper.getAudioFileDuration(AUDIO_FILE_MP3_PATH) > 0);
        } catch (IOException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    public void testUserSubmitsAudioFileLowerThanLimitSize() {
        try {
            long fileSize = AudioFileHelper.getAudioFileSizeInBytes(AUDIO_FILE_MP3_PATH);
            int maxFileSize = ConfigLoader.getMaxFileSizeInBytes();
            assertTrue(fileSize <= maxFileSize);
        } catch (IOException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    public void testSystemExtractsAPIKeyFromTxtFile() {
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

        // Verify the Request object has the valid attributes
        assertFalse(capturedRequest.url().toString().isEmpty());
        assertEquals("Bearer valid-api-key", capturedRequest.header("Authorization"));
        assertNotNull(capturedRequest.header("OpenAI-Organization"));

        // Verify the Response
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testWithValidApiKeyTranscribeAudioFile() throws IOException {
        String validApiKey = "valid-api-key";
        String someLanguage = "en";

        // Mock dependencies
        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);

        // Mock the response
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("https://some-url").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200) // simulate HTTP 200
                .message("OK")
                .body(ResponseBody.Companion.create(
                        "{\"text\": \"some transcribed text\"}",
                        MediaType.get("application/json; charset=utf-8"))
                )
                .build();

        when(mockCall.execute()).thenReturn(mockResponse);

        // Instantiate the service with the mocked client
        WhisperApiService service = new WhisperApiService(mockClient);

        // Execute the method
        String result = service.transcribeAudioFile(validApiKey, someLanguage, AUDIO_FILE_MP3_PATH);

        // Verify results and interactions
        assertNotNull(result);
        assertEquals("{\"text\": \"some transcribed text\"}", result);
    }
}
