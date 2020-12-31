package com.github.igorperikov.botd.restart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LocalFileMd5Storage implements Md5Storage {
    private static final Path LOCAL_FILE_PATH = Path.of(
            "/",
            "Users",
            "igorperikov",
            "idea_projects",
            "botd_spotify_integration",
            "src",
            "main",
            "resources",
            "md5"
    );

    public LocalFileMd5Storage() {
        try {
            if (Files.notExists(LOCAL_FILE_PATH)) {
                Files.createFile(LOCAL_FILE_PATH);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(String md5) {
        try {
            Files.deleteIfExists(LOCAL_FILE_PATH);
            Files.writeString(LOCAL_FILE_PATH, md5);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String read() {
        try {
            List<String> strings = Files.readAllLines(LOCAL_FILE_PATH);
            if (strings.isEmpty()) return "";
            return strings.get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
