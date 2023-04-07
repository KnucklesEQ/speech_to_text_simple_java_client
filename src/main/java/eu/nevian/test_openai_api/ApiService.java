package eu.nevian.test_openai_api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class ApiService {
    private final OkHttpClient httpClient;

    public ApiService() {
        httpClient = new OkHttpClient();
    }

    public String fetchPosts() throws IOException {
        String url = "https://jsonplaceholder.typicode.com/posts";
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String aux = "";
            if (response.body() != null) {
                aux = response.body().string();
            }

            return aux;
        } catch (Exception e) {
            throw new IOException("Unexpected response code: " + e);
        }
    }
}
