package eu.nevian.speech_to_text_simple_java_client;

import java.io.IOException;

public class Main {
    private final static String apiKey = "YOUR_API_KEY";
    private final static String audioFilePath = "THE_PATH_TO_YOUR_AUDIO_FILE";

    public static void main(String[] args) {
        System.out.println("Welcome!");

        ApiService apiService = new ApiService();

        try {
            System.out.println();
            System.out.println("###### Trying access to OpenAI API ######");
            String responseText = apiService.checkWhisperOpenAiModel(apiKey);
            System.out.println("API Response: ");
            System.out.println(responseText);

            System.out.println();
            System.out.println("###### Transcribe audio to text ######");
            String audioTranscription = apiService.transcribeAudioFile(apiKey, audioFilePath);
            System.out.println("Text: ");
            System.out.println(audioTranscription);
        } catch (IOException e) {
            System.err.println("Error fetching data from API: " + e.getMessage());
        }
    }
}
