package eu.nevian.speech_to_text_simple_java_client.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class TemporaryWorkspaceHelper implements AutoCloseable {
    private static final String TEMP_DIR_PREFIX = "sttsjc-";

    private final Path workspacePath;

    private TemporaryWorkspaceHelper(Path workspacePath) {
        this.workspacePath = workspacePath;
    }

    public static TemporaryWorkspaceHelper createTemporaryWorkspace() throws IOException {
        return new TemporaryWorkspaceHelper(Files.createTempDirectory(TEMP_DIR_PREFIX));
    }

    public Path getWorkspacePath() {
        return workspacePath;
    }

    @Override
    public void close() {
        if (workspacePath == null || !Files.exists(workspacePath)) {
            return;
        }

        try (var paths = Files.walk(workspacePath)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    System.err.println("Warning: Failed to delete temporary file: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("Warning: Failed to delete temporary workspace: " + e.getMessage());
        }
    }
}
