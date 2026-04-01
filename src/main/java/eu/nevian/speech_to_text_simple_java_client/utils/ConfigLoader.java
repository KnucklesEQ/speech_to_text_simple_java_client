package eu.nevian.speech_to_text_simple_java_client.utils;

import eu.nevian.speech_to_text_simple_java_client.config.ApplicationDefaults;
import eu.nevian.speech_to_text_simple_java_client.config.UserConfig;
import eu.nevian.speech_to_text_simple_java_client.exceptions.LoadingConfigurationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public class ConfigLoader {
    private static final String APPLICATION_DEFAULTS_RESOURCE = "application-defaults.properties";
    private static final String API_KEY_PROPERTY = "api_key";
    private static final String LANGUAGE_PROPERTY = "language";
    private static final String DEFAULT_LANGUAGE_PROPERTY = "default_language";
    private static final String AUDIO_FILE_LIMIT_SIZE_PROPERTY = "audio_file_limit_size_in_bytes";

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

    public static UserConfig loadUserConfig(Path configFilePath) throws IOException, LoadingConfigurationException {
        Properties properties = loadPropertiesFromFile(configFilePath);

        return new UserConfig(
                readRequiredProperty(properties, API_KEY_PROPERTY, MessageManager.getApiKeyNotFoundMessage()),
                normalizeLanguage(properties.getProperty(LANGUAGE_PROPERTY))
        );
    }

    public static ApplicationDefaults loadApplicationDefaults() throws IOException, LoadingConfigurationException {
        Properties properties = loadPropertiesFromClasspath(APPLICATION_DEFAULTS_RESOURCE);

        String defaultLanguage = normalizeLanguage(
                readRequiredProperty(properties, DEFAULT_LANGUAGE_PROPERTY, invalidDefaultsMessage(DEFAULT_LANGUAGE_PROPERTY))
        );
        if (!LanguageSupport.isValidLanguageCode(defaultLanguage)) {
            throw new LoadingConfigurationException(invalidDefaultsMessage(DEFAULT_LANGUAGE_PROPERTY));
        }

        return new ApplicationDefaults(
                defaultLanguage,
                readRequiredPositiveLongProperty(properties, AUDIO_FILE_LIMIT_SIZE_PROPERTY)
        );
    }

    public static void saveLanguage(Path configFilePath, String language) throws IOException, LoadingConfigurationException {
        Properties properties = loadPropertiesFromFile(configFilePath);

        readRequiredProperty(properties, API_KEY_PROPERTY, MessageManager.getApiKeyNotFoundMessage());

        properties.setProperty(LANGUAGE_PROPERTY, normalizeLanguage(language));

        try (FileOutputStream fileOutputStream = new FileOutputStream(configFilePath.toFile())) {
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            throw new IOException(MessageManager.getConfigFileWriteErrorMessage(e.getMessage()), e);
        }
    }

    private static Properties loadPropertiesFromFile(Path configFilePath) throws IOException {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(configFilePath.toFile())) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(MessageManager.getConfigFileNotFoundMessage(configFilePath.toString()));
        } catch (IOException e) {
            throw new IOException(MessageManager.getConfigFileReadErrorMessage(e.getMessage()), e);
        }

        return properties;
    }

    private static Properties loadPropertiesFromClasspath(String resourceName) throws IOException, LoadingConfigurationException {
        Properties properties = new Properties();

        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new LoadingConfigurationException(
                        MessageManager.getApplicationDefaultsNotFoundMessage(resourceName)
                );
            }

            properties.load(inputStream);
        } catch (IOException e) {
            throw new IOException(MessageManager.getApplicationDefaultsReadErrorMessage(resourceName, e.getMessage()), e);
        }

        return properties;
    }

    private static String readRequiredProperty(Properties properties, String propertyName, String errorMessage) {
        String value = normalizeValue(properties.getProperty(propertyName));
        if (value == null) {
            throw new LoadingConfigurationException(errorMessage);
        }

        return value;
    }

    private static long readRequiredPositiveLongProperty(Properties properties, String propertyName) {
        String value = readRequiredProperty(properties, propertyName, invalidDefaultsMessage(propertyName));

        long parsedValue;
        try {
            parsedValue = Long.parseLong(value.replace("_", ""));
        } catch (NumberFormatException e) {
            throw new LoadingConfigurationException(invalidDefaultsMessage(propertyName));
        }

        if (parsedValue <= 0) {
            throw new LoadingConfigurationException(invalidDefaultsMessage(propertyName));
        }

        return parsedValue;
    }

    private static String invalidDefaultsMessage(String propertyName) {
        return MessageManager.getApplicationDefaultsInvalidMessage(APPLICATION_DEFAULTS_RESOURCE, propertyName);
    }

    private static String normalizeLanguage(String language) {
        String normalizedLanguage = normalizeValue(language);
        return normalizedLanguage == null ? null : normalizedLanguage.toLowerCase(Locale.ROOT);
    }

    private static String normalizeValue(String value) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();
        return normalizedValue.isEmpty() ? null : normalizedValue;
    }
}
