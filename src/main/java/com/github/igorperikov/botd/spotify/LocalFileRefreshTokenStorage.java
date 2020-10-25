package com.github.igorperikov.botd.spotify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LocalFileRefreshTokenStorage implements RefreshTokenStorage {
    private static final Path LOCAL_FILE_PATH = Path.of(
            "/",
            "Users",
            "baltor",
            "idea_projects_personal",
            "botd_spotify_integration",
            "src",
            "main",
            "resources",
            "refresh_token"
    );

    @Override
    public String get() {
        try {
            return read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String refreshToken) {
        try {
            write(refreshToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void write(String refreshToken) throws IOException {
        Files.deleteIfExists(LOCAL_FILE_PATH);
        Files.writeString(LOCAL_FILE_PATH, refreshToken);
    }

    private synchronized String read() throws IOException {
        List<String> strings = Files.readAllLines(LOCAL_FILE_PATH);
        return strings.get(0);
    }
}
