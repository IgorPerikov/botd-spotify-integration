package com.github.igorperikov.botd.data.domain;

import java.util.ArrayList;
import java.util.List;

public class BotdStage {
    private final int index;
    private final BotdUser author;
    private final String band;
    private final List<BotdTrack> tracks = new ArrayList<>();

    public BotdStage(int index, BotdUser author, String band) {
        this.index = index;
        this.author = author;
        this.band = band;
    }

    public int getIndex() {
        return index;
    }

    public BotdUser getAuthor() {
        return author;
    }

    public String getBand() {
        return band;
    }

    public List<BotdTrack> getTracks() {
        return tracks;
    }
}
