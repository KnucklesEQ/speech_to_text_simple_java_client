package eu.nevian.speech_to_text_simple_java_client.utils;

public enum FileType {
    AUDIO("audio"),
    VIDEO("video");

    private final String type;

    FileType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
