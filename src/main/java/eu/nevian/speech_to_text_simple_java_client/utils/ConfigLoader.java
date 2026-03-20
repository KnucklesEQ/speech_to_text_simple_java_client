package eu.nevian.speech_to_text_simple_java_client.utils;

import eu.nevian.speech_to_text_simple_java_client.exceptions.LoadingConfigurationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigLoader {
    private ConfigLoader() {
    }

    public static Path resolveConfigFilePath(String configFileName) {
        Path currentWorkingDirectoryConfigPath = Path.of(configFileName).toAbsolutePath().normalize();

        try {
            if (ConfigLoader.class.getProtectionDomain() == null
                    || ConfigLoader.class.getProtectionDomain().getCodeSource() == null
                    || ConfigLoader.class.getProtectionDomain().getCodeSource().getLocation() == null) {
                return currentWorkingDirectoryConfigPath;
            }

            Path codeSourcePath = Path.of(
                    ConfigLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            ).toAbsolutePath().normalize();

            if (Files.isRegularFile(codeSourcePath)
                    && codeSourcePath.getFileName() != null
                    && codeSourcePath.getFileName().toString().endsWith(".jar")) {
                Path jarDirectoryPath = codeSourcePath.getParent();
                if (jarDirectoryPath != null) {
                    return jarDirectoryPath.resolve(configFileName).toAbsolutePath().normalize();
                }
            }
        } catch (URISyntaxException | RuntimeException e) {
            return currentWorkingDirectoryConfigPath;
        }

        return currentWorkingDirectoryConfigPath;
    }

    public static boolean configFileDoesNotExist(Path configFilePath) {
        return !Files.exists(configFilePath);
    }

    public static String getApiKey(String ApiKeyFilePath) throws IOException, LoadingConfigurationException {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(ApiKeyFilePath)) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(MessageManager.getConfigFileNotFoundMessage(ApiKeyFilePath));
        } catch (IOException e) {
            throw new IOException(MessageManager.getConfigFileReadErrorMessage(e.getMessage()));
        }

        String apiKey = properties.getProperty("api_key");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new LoadingConfigurationException(MessageManager.getApiKeyNotFoundMessage());
        }

        return apiKey;
    }

    public static int getMaxFileSizeInBytes(String configFilePath) throws IOException {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(configFilePath)) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(MessageManager.getConfigFileNotFoundMessage(configFilePath));
        } catch (IOException e) {
            throw new IOException(MessageManager.getConfigFileReadErrorMessage(e.getMessage()));
        }

        String fileSizeString = properties.getProperty("audio_file_limit_size_in_bytes");
        if (fileSizeString == null || fileSizeString.isEmpty()) {
            throw new LoadingConfigurationException(MessageManager.getApiKeyNotFoundMessage());
        }

        return Integer.parseInt(fileSizeString.replace("_", ""));
    }

    public static String getLanguage(String configFilePath) throws IOException {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(configFilePath)) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            return null;
        }

        String language = properties.getProperty("language");
        if (language == null || language.trim().isEmpty()) {
            return null;
        }

        return language.trim();
    }

    public static void saveLanguage(String configFilePath, String language) throws IOException {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(configFilePath)) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(MessageManager.getConfigFileNotFoundMessage(configFilePath));
        }

        properties.setProperty("language", language);

        try (FileOutputStream fileOutputStream = new FileOutputStream(configFilePath)) {
            properties.store(fileOutputStream, null);
        }
    }
}
