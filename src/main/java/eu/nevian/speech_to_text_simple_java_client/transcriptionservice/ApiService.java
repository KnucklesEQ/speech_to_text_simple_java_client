package eu.nevian.speech_to_text_simple_java_client.transcriptionservice;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is responsible for making API calls to OpenAI API.
 */
public class ApiService {
    /**
     * HTTP client used for making API calls.
     */
    private final OkHttpClient httpClient;

    /**
     * Constructor. Creates a new instance of {@link ApiService} and configures the timeout values for the HTTP client.
     */
    public ApiService() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Performs an API call to OpenAI API to check if the Whisper model is available.
     *
     * @param apiKey API key to use for the API call.
     * @return Response body as a string.
     * @throws IOException If an error occurs while making the API call.
     */
    public String checkWhisperOpenAiModel(String apiKey) throws IOException {
        final String organization = "org-GsWPyLdc05pSY3GVSQt2dWkP";
        final String url = "https://api.openai.com/v1/models/whisper-1";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("OpenAI-Organization", organization)
                .build();

        try (Response response = performApiRequestWithAnimation(request)) {
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
        final String url = "https://api.openai.com/v1/audio/transcriptions";
        final String model = "whisper-1";

        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            throw new IOException("Audio file not found: " + audioFilePath);
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file", audioFile.getName(),
                        RequestBody.create(audioFile, MediaType.get("audio/mpeg")))
                .addFormDataPart("model", model)
                .addFormDataPart("language", language)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();

        try (Response response = performApiRequestWithAnimation(request)) {
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
     * Performs an API request and shows a loading animation while waiting for the response.
     *
     * @param request Request to perform.
     * @return Response from the API.
     * @throws IOException If an error occurs while making the API call.
     */
    private Response performApiRequestWithAnimation(Request request) throws IOException {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        char[] spinnerChars = new char[]{'|', '/', '-', '\\'};
        AtomicInteger spinnerIndex = new AtomicInteger(0);

        Runnable loadingTask = () ->
                System.out.print("\rLoading... " + spinnerChars[spinnerIndex.getAndIncrement() % spinnerChars.length]);

        // Schedule the loadingTask to run every 100 milliseconds
        executor.scheduleAtFixedRate(loadingTask, 0, 100, TimeUnit.MILLISECONDS);

        try {
            return httpClient.newCall(request).execute();
        } finally {
            // Shutdown the executor always
            executor.shutdown();
        }
    }
}
