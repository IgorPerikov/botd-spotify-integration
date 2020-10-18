package com.github.igorperikov.botd.domain;

import java.util.List;

public class BotdData {
    private final List<BotdStage> botdStages;
    private final BotdUsers users;

    public BotdData(List<BotdStage> botdStages, BotdUsers users) {
        this.botdStages = botdStages;
        this.users = users;
    }

    public List<BotdStage> getBotdStages() {
        return botdStages;
    }

    public BotdUsers getUsers() {
        return users;
    }
}
