package eu.nevian.speech_to_text_simple_java_client.transcriptionservice;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiServiceHelper {
    /**
     * Performs an API request and shows a loading animation while waiting for the response.
     *
     * @param request Request to perform.
     * @return Response from the API.
     * @throws IOException If an error occurs while making the API call.
     */
    public static Response performApiRequestWithAnimation(Request request, OkHttpClient httpClient) throws IOException {
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
