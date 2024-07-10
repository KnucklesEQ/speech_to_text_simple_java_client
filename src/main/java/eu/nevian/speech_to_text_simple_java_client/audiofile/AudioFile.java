package eu.nevian.speech_to_text_simple_java_client.audiofile;

import eu.nevian.speech_to_text_simple_java_client.utils.FileType;

/**
 * Represents a file containing an audio element.
 */
public class AudioFile {
    /** Path to the file. */
    private String filePath;

    /**
     * This var reflects the type of file we are working with (audio or video)
     */
    private FileType fileType;

    /** Audio duration in seconds. */
    private double duration;

    /** File size in bytes. */
    private long fileSize;

    /**
     * Default constructor.
     */
    public AudioFile() {
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public double getDuration() {
        return duration;
    }

    public double getDurationInMinutes() {
        return duration / 60;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public double getFileSizeInMB() {
        return ((fileSize / 1024.0) / 1024.0);
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "AudioFile{" +
                "filePath='" + filePath + '\'' +
                ", fileType='" + fileType + '\'' +
                ", duration=" + String.format("%.2f", getDurationInMinutes()) + " minutes" +
                ", fileSize=" + String.format("%.2f", getFileSizeInMB()) + " MB" +
                '}';
    }
}
