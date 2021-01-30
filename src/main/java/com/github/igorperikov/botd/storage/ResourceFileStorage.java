package com.github.igorperikov.botd.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ResourceFileStorage {
    private final Path pathToFile;

    public ResourceFileStorage(String resourceName) {
        this.pathToFile = Path.of(
                "/",
                "Users",
                "igorperikov",
                "idea_projects",
                "botd_spotify_integration",
                "src",
                "main",
                "resources",
                resourceName
        );
    }

    public synchronized List<String> readAll() {
        try {
            return Files.readAllLines(pathToFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized String readStringOrEmpty() {
        List<String> strings = readAll();
        if (strings.isEmpty()) return "";
        return strings.get(0);
    }

    public synchronized void replaceWithNewContent(String content) {
        try {
            Files.deleteIfExists(pathToFile);
            Files.writeString(pathToFile, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void append(String string) {
        try {
            Files.writeString(pathToFile, string, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void recreateFile() {
        try {
            Files.deleteIfExists(pathToFile);
            Files.createFile(pathToFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void createIfNotExists() {
        try {
            if (Files.notExists(pathToFile)) {
                Files.createFile(pathToFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
