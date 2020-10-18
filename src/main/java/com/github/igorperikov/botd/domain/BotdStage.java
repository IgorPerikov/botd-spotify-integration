package com.github.igorperikov.botd.domain;

import java.util.List;
import java.util.Map;

public class BotdStage {
    private final int index;
    private final BotdUser author;
    private final Map<String, List<BotdTrack>> tracks;

    public BotdStage(int index, BotdUser author, Map<String, List<BotdTrack>> tracks) {
        this.index = index;
        this.author = author;
        this.tracks = tracks;
    }
}
