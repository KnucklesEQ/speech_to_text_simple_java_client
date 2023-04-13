package eu.nevian.speech_to_text_simple_java_client.audiofile;

public class AudioFile {
    private String filePath;
    private String fileType;
    private double duration;

    public AudioFile(){}

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
