package eu.nevian.speech_to_text_simple_java_client.transcriptionservice;

import java.io.IOException;

/**
 * This interface is responsible for making API calls to OpenAI API.
 */
public interface ApiService {
    String checkAiModelIsAvailable(String apiKey) throws IOException;
    String transcribeAudioFile(String apiKey, String language, String audioFilePath) throws IOException;
}
