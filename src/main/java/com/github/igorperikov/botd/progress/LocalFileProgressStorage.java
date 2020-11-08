package com.github.igorperikov.botd.progress;

import com.github.igorperikov.botd.data.domain.BotdTrack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalFileProgressStorage implements ProgressStorage {
    private static final Path LOCAL_FILE_PATH = Path.of(
            "/",
            "Users",
            "baltor",
            "idea_projects_personal",
            "botd_spotify_integration",
            "src",
            "main",
            "resources",
            "progress"
    );

    @Override
    public boolean isProcessed(BotdTrack botdTrack) {
        try {
            return read().contains(botdTrack.getGlobalIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markAsProcessed(BotdTrack botdTrack) {
        try {
            write(botdTrack.getGlobalIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void write(int globalIndex) throws IOException {
        Set<Integer> currentIndexes = read();
        currentIndexes.add(globalIndex);
        String setRepresentation = currentIndexes.stream().map(Object::toString).collect(Collectors.joining(","));
        Files.deleteIfExists(LOCAL_FILE_PATH);
        Files.writeString(LOCAL_FILE_PATH, setRepresentation);
    }

    private synchronized Set<Integer> read() throws IOException {
        List<String> strings = Files.readAllLines(LOCAL_FILE_PATH);
        if (strings.isEmpty()) {
            return new HashSet<>();
        }
        String setString = strings.get(0);
        return Arrays.stream(setString.split(",")).mapToInt(Integer::parseInt).boxed().collect(Collectors.toSet());
    }
}
