package eu.nevian.speech_to_text_simple_java_client.audiofile;

public class AudioFile {
    private String filePath;

    private String fileType;

    /** Audio duration in seconds. */
    private double duration;

    /** File size in bytes. */
    private long fileSize;

    /** Default constructor. */
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "AudioFile{" +
                "filePath='" + filePath + '\'' +
                ", fileType='" + fileType + '\'' +
                ", duration=" + duration +
                ", fileSize=" + fileSize +
                '}';
    }
}
