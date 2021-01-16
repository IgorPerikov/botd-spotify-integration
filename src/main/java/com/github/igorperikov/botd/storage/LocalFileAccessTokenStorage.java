package com.github.igorperikov.botd.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// TODO: unify local file storages
public class LocalFileAccessTokenStorage implements AccessTokenStorage {
    private static final Path LOCAL_FILE_PATH = Path.of(
            "/",
            "Users",
            "igorperikov",
            "idea_projects",
            "botd_spotify_integration",
            "src",
            "main",
            "resources",
            "access_token.txt"
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
    public void update(String accessToken) {
        try {
            write(accessToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: unnecessary synchronized?
    private synchronized void write(String accessToken) throws IOException {
        Files.deleteIfExists(LOCAL_FILE_PATH);
        Files.writeString(LOCAL_FILE_PATH, accessToken);
    }

    private synchronized String read() throws IOException {
        List<String> strings = Files.readAllLines(LOCAL_FILE_PATH);
        return strings.get(0);
    }
}
