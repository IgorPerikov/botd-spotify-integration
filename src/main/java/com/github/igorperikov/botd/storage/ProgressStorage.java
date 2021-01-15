package com.github.igorperikov.botd.storage;

import com.github.igorperikov.botd.entity.BotdData;
import com.github.igorperikov.botd.entity.BotdTrack;

import java.util.List;
import java.util.stream.Collectors;

public interface ProgressStorage {
    boolean isProcessed(BotdTrack botdTrack);

    void markAsProcessed(BotdTrack botdTrack);

    void deleteAllProgress();

    default List<BotdTrack> getAllProcessed(BotdData botdData) {
        return botdData.getAllTracks()
                .stream()
                .filter(this::isProcessed)
                .collect(Collectors.toList());
    }

    default List<BotdTrack> getAllUnprocessed(BotdData botdData) {
        return botdData.getAllTracks()
                .stream()
                .filter(botdTrack -> !isProcessed(botdTrack))
                .collect(Collectors.toList());
    }
}
