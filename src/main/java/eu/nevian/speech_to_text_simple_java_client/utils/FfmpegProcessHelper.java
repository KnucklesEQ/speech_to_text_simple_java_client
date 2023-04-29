package eu.nevian.speech_to_text_simple_java_client.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FfmpegProcessHelper {
    private static final String AUDIO_CODEC = "libmp3lame";
    private static final String AUDIO_BITRATE = "64k";

    private FfmpegProcessHelper() {
    }

    /**
     * Check if ffmpeg is NOT available on the system.
     *
     * @return True if ffmpeg is NOT available, false otherwise.
     */
    public static boolean isFfmpegNotAvailable() {
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-version");
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode != 0;
        } catch (IOException | InterruptedException e) {
            return true;
        }
    }

    public static ProcessBuilder createFfmpegProcessBuilder(String... args) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.addAll(Arrays.asList(args));

        return new ProcessBuilder(command);
    }

    public static ProcessBuilder createFfprobeProcessBuilder(String... args) {
        List<String> command = new ArrayList<>();
        command.add("ffprobe");
        command.addAll(Arrays.asList(args));

        return new ProcessBuilder(command);
    }

    public static ProcessBuilder createExtractAudioProcessBuilder(String inputFilePath, String outputFilePath) {
        // -y -> Overwrite without asking for confirmation the output file if it already exists
        // -i -> The input file
        // -vn -> Disable video (only audio stream will be processed)
        // -acodec -> The audio codec to use (libmp3lame)
        // -b:a -> Sets the audio bitrate of the output file (64k)
        return createFfmpegProcessBuilder(
                "-y",
                "-i", inputFilePath,
                "-vn",
                "-acodec", AUDIO_CODEC,
                "-b:a", AUDIO_BITRATE,
                outputFilePath
        );
    }

    public static ProcessBuilder createCutAudioProcessBuilder(String inputFilePath, String outputFilePath, double startTime, double duration) {
        // -y -> Overwrite without asking for confirmation the output file if it already exists
        // -i -> The input file
        // -ss -> The start time (in seconds) of the part to extract
        // -t -> The duration (in seconds) of the part to extract
        // -vn -> Disable video (only audio stream will be processed)
        // -acodec -> The audio codec to use (libmp3lame)
        // -b:a -> Sets the audio bitrate of the output file (64k)
        return createFfmpegProcessBuilder(
                "-y",
                "-i", inputFilePath,
                "-ss", String.valueOf(startTime),
                "-t", String.valueOf(duration),
                "-vn",
                "-acodec", AUDIO_CODEC,
                "-b:a", AUDIO_BITRATE,
                outputFilePath
        );
    }

    public static ProcessBuilder createGetAudioDurationProcessBuilder(String inputFilePath) {
        // -v error -> Set the log level to "error" to suppress unnecessary messages
        // -show_entries format=duration -> Show only the duration entry from the format section
        // -of json -> Set the output format to JSON.
        return createFfprobeProcessBuilder(
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "json",
                inputFilePath
        );
    }
}
