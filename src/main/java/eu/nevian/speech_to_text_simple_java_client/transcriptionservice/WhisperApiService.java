package eu.nevian.speech_to_text_simple_java_client.transcriptionservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WhisperApiService implements ApiService{
    /**
     * HTTP client used for making API calls.
     */
    private final OkHttpClient httpClient;
    private final TranscriptionServiceDefinition transcriptionServiceDefinition;

    /**
     * Constructor. Creates a new instance of {@link WhisperApiService} piService} and configures the timeout values for
     * the HTTP client.
     */
    public WhisperApiService() {
        this(createDefaultHttpClient(), TranscriptionServiceDefinition.OPENAI_WHISPER);
    }

    public WhisperApiService(OkHttpClient httpClient) {
        this(httpClient, TranscriptionServiceDefinition.OPENAI_WHISPER);
    }

    public WhisperApiService(TranscriptionServiceDefinition transcriptionServiceDefinition) {
        this(createDefaultHttpClient(), transcriptionServiceDefinition);
    }

    public WhisperApiService(OkHttpClient httpClient, TranscriptionServiceDefinition transcriptionServiceDefinition) {
        this.httpClient = httpClient;
        this.transcriptionServiceDefinition = transcriptionServiceDefinition;
    }

    /**
     * Performs an API call to OpenAI API to check if the Whisper model is available.
     *
     * @param apiKey API key to use for the API call.
     * @return Response body as a string.
     * @throws IOException If an error occurs while making the API call.
     */
    public String checkAiModelIsAvailable(String apiKey) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(transcriptionServiceDefinition.modelCheckUrl())
                .addHeader("Authorization", "Bearer " + apiKey)
                .get();

        if (transcriptionServiceDefinition.organization() != null) {
            requestBuilder.addHeader("OpenAI-Organization", transcriptionServiceDefinition.organization());
        }

        Request request = requestBuilder.build();

        try (Response response = ApiServiceHelper.performApiRequestWithAnimation(request, httpClient)) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            if (response.body() == null) {
                throw new IOException("Response body is null");
            }

            return response.body().string();
        }
    }

    /**
     * Performs an API call to OpenAI API to transcribe an audio file.
     *
     * @param apiKey        API key to use for the API call.
     * @param audioFilePath Path to the audio file to transcribe.
     * @return Response body as a string with the content of the transcription.
     * @throws IOException If an error occurs while making the API call.
     */
    public String transcribeAudioFile(String apiKey, String language, String audioFilePath) throws IOException {
        File file = new File(audioFilePath);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file", file.getName(),
                        RequestBody.create(file, MediaType.get("audio/mpeg")))
                .addFormDataPart("model", transcriptionServiceDefinition.modelName())
                .addFormDataPart("language", language)
                .build();

        Request request = new Request.Builder()
                .url(transcriptionServiceDefinition.transcriptionUrl())
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "multipart/form-data")
                .post(requestBody) // POST request
                .build();

        try (Response response = ApiServiceHelper.performApiRequestWithAnimation(request, httpClient)) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            if (response.body() == null) {
                throw new IOException("Response body is null");
            }

            String responseBody = response.body().string();
            return extractTranscriptionText(responseBody);
        }
    }

    private String extractTranscriptionText(String responseBody) throws IOException {
        JsonNode rootNode;

        try {
            rootNode = new ObjectMapper().readTree(responseBody);
        } catch (IOException e) {
            throw new IOException("Invalid transcription response format", e);
        }

        JsonNode textNode = rootNode.path("text");
        if (textNode.isMissingNode() || textNode.isNull() || !textNode.isTextual()) {
            throw new IOException("Invalid transcription response: missing text field");
        }

        return textNode.asText();
    }

    private static OkHttpClient createDefaultHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .build();
    }

}
