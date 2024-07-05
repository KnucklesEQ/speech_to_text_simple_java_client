package eu.nevian.speech_to_text_simple_java_client.utils;

import eu.nevian.speech_to_text_simple_java_client.exceptions.LoadingConfigurationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private ConfigLoader() {
    }

    public static String getApiKey(String ApiKeyFilePath) throws IOException, LoadingConfigurationException {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(ApiKeyFilePath)) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(MessageManager.getConfigFileNotFoundMessage());
        } catch (IOException e) {
            throw new IOException(MessageManager.getConfigFileReadErrorMessage(e.getMessage()));
        }

        String apiKey = properties.getProperty("api_key");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new LoadingConfigurationException(MessageManager.getApiKeyNotFoundMessage());
        }

        return apiKey;
    }
}
