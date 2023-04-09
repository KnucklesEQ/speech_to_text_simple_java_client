package eu.nevian.test_openai_api;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiService {
    private final OkHttpClient httpClient;

    public ApiService() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .build();
    }

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

            if(response.body() == null) {
                throw new IOException("Response body is null");
            }

            return response.body().string();
        }
    }

    public String transcribeAudioFile(String apiKey, String audioFilePath) throws IOException {
        final String url = "https://api.openai.com/v1/audio/transcriptions";
        final String model = "whisper-1";

        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            throw new IOException("Audio file not found: " + audioFilePath);
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(),
                        RequestBody.create(audioFile, MediaType.get("audio/mpeg")))
                .addFormDataPart("model", model)
                .addFormDataPart("language", "en")
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

            if(response.body() == null) {
                throw new IOException("Response body is null");
            }
            return response.body().string();
        }
    }

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
