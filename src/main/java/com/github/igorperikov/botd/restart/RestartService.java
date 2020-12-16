package com.github.igorperikov.botd.restart;

import com.github.igorperikov.botd.entity.BotdData;

public interface RestartService {
    boolean restartRequired(BotdData botdData);
}
