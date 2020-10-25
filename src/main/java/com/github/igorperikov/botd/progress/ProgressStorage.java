package com.github.igorperikov.botd.progress;

import com.github.igorperikov.botd.domain.BotdTrack;

public interface ProgressStorage {
    boolean isProcessed(BotdTrack botdTrack);

    void markAsProcessed(BotdTrack botdTrack);
}
