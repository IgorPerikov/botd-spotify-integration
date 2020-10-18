package com.github.igorperikov.botd.domain;

public class BotdTrack {
    private final int globalIndex;
    private final String band;
    private final String track;

    public BotdTrack(int globalIndex, String band, String track) {
        this.globalIndex = globalIndex;
        this.band = band;
        this.track = track;
    }
}
