package eu.nevian.test_openai_api;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        ApiService apiService = new ApiService();

        try {
            String postsJson = apiService.fetchPosts();
            System.out.println("API Response: ");
            System.out.println(postsJson);
        } catch (IOException e) {
            System.err.println("Error fetching data from API: " + e.getMessage());
        }
    }
}
