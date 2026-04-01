package eu.nevian.speech_to_text_simple_java_client.config;

import eu.nevian.speech_to_text_simple_java_client.commandlinemanagement.CommandLineOptions;
import eu.nevian.speech_to_text_simple_java_client.exceptions.InvalidLanguageOptionException;
import eu.nevian.speech_to_text_simple_java_client.exceptions.LoadingConfigurationException;
import eu.nevian.speech_to_text_simple_java_client.transcriptionservice.TranscriptionServiceDefinition;
import eu.nevian.speech_to_text_simple_java_client.utils.ConfigLoader;
import eu.nevian.speech_to_text_simple_java_client.utils.LanguageSupport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public final class ApplicationConfigResolver {
    private static final String INVALID_LANGUAGE_CODE_MESSAGE = "Error: Invalid language code";

    private ApplicationConfigResolver() {
    }

    public static ResolvedApplicationConfig resolve(
            CommandLineOptions cmdOptions,
            Path configFilePath,
            UserConfig userConfig,
            ApplicationDefaults applicationDefaults
    ) throws InvalidLanguageOptionException {
        LanguageResolution languageResolution = resolveLanguage(cmdOptions, configFilePath, userConfig, applicationDefaults);

        return new ResolvedApplicationConfig(
                userConfig.apiKey(),
                languageResolution.effectiveLanguage(),
                applicationDefaults.audioFileLimitSizeInBytes(),
                TranscriptionServiceDefinition.OPENAI_WHISPER,
                languageResolution.warningMessage()
        );
    }

    private static LanguageResolution resolveLanguage(
            CommandLineOptions cmdOptions,
            Path configFilePath,
            UserConfig userConfig,
            ApplicationDefaults applicationDefaults
    ) throws InvalidLanguageOptionException {
        String languageOption = cmdOptions.getLanguageOption();
        if (languageOption != null) {
            String normalizedLanguage = languageOption.trim().toLowerCase(Locale.ROOT);
            if (!LanguageSupport.isValidLanguageCode(normalizedLanguage)) {
                throw new InvalidLanguageOptionException(INVALID_LANGUAGE_CODE_MESSAGE);
            }

            try {
                ConfigLoader.saveLanguage(configFilePath, normalizedLanguage);
                return new LanguageResolution(normalizedLanguage, null);
            } catch (IOException | LoadingConfigurationException e) {
                return new LanguageResolution(
                        normalizedLanguage,
                        "Warning: Failed to save language to config.properties: " + e.getMessage()
                );
            }
        }

        String configuredLanguage = userConfig.language();
        if (configuredLanguage != null) {
            if (!LanguageSupport.isValidLanguageCode(configuredLanguage)) {
                return new LanguageResolution(
                        applicationDefaults.defaultLanguage(),
                        "Warning: Invalid language in config.properties (" + configuredLanguage
                                + "). Falling back to \"" + applicationDefaults.defaultLanguage() + "\"."
                );
            }

            return new LanguageResolution(configuredLanguage, null);
        }

        return new LanguageResolution(applicationDefaults.defaultLanguage(), null);
    }

    private record LanguageResolution(String effectiveLanguage, String warningMessage) {
    }
}
