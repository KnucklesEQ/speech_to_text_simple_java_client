package eu.nevian.speech_to_text_simple_java_client.transcriptionservice;

public record TranscriptionServiceDefinition(
        String modelName,
        String modelCheckUrl,
        String transcriptionUrl,
        String organization
) {
    public static final TranscriptionServiceDefinition OPENAI_WHISPER = new TranscriptionServiceDefinition(
            "whisper-1",
            "https://api.openai.com/v1/models/whisper-1",
            "https://api.openai.com/v1/audio/transcriptions",
            "org-GsWPyLdc05pSY3GVSQt2dWkP"
    );
}
