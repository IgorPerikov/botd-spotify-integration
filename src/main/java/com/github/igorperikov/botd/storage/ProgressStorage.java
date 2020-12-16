package com.github.igorperikov.botd.storage;

import com.github.igorperikov.botd.entity.BotdTrack;

public interface ProgressStorage {
    boolean isProcessed(BotdTrack botdTrack);

    void markAsProcessed(BotdTrack botdTrack);

    void deleteAllProgress();
}
