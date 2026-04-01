package eu.nevian.speech_to_text_simple_java_client.config;

import eu.nevian.speech_to_text_simple_java_client.transcriptionservice.TranscriptionServiceDefinition;

public record ResolvedApplicationConfig(
        String apiKey,
        String effectiveLanguage,
        long audioFileLimitSizeInBytes,
        TranscriptionServiceDefinition serviceDefinition,
        String warningMessage
) {
}
