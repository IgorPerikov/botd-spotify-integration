package com.github.igorperikov.botd.storage;

import com.github.igorperikov.botd.entity.BotdData;
import com.github.igorperikov.botd.entity.BotdTrack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceFileProgressStorage extends ResourceFileStorage implements ProgressStorage {
    public ResourceFileProgressStorage() {
        super("progress.txt");
    }

    @Override
    public boolean isProcessed(BotdTrack botdTrack) {
        return read().contains(botdTrack.getGlobalIndex());
    }

    @Override
    public void markAsProcessed(BotdTrack botdTrack) {
        write(botdTrack.getGlobalIndex());
    }

    @Override
    public void deleteAllProgress() {
        recreateFile();
    }

    @Override
    public List<BotdTrack> getAllProcessed(BotdData botdData) {
        Set<Integer> processed = read();
        return botdData.getAllTracks()
                .stream()
                .filter(botdTrack -> processed.contains(botdTrack.getGlobalIndex()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BotdTrack> getAllUnprocessed(BotdData botdData) {
        Set<Integer> processed = read();
        return botdData.getAllTracks()
                .stream()
                .filter(botdTrack -> !processed.contains(botdTrack.getGlobalIndex()))
                .collect(Collectors.toList());
    }

    private void write(int globalIndex) {
        Set<Integer> currentIndexes = read();
        currentIndexes.add(globalIndex);
        String setRepresentation = currentIndexes.stream().map(Object::toString).collect(Collectors.joining(","));
        replaceWithNewContent(setRepresentation);
    }

    private Set<Integer> read() {
        List<String> strings = readAll();
        if (strings.isEmpty()) {
            return new HashSet<>();
        }
        String setString = strings.get(0);
        return Arrays.stream(setString.split(",")).mapToInt(Integer::parseInt).boxed().collect(Collectors.toSet());
    }
}
