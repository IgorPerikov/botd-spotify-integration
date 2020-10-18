package com.github.igorperikov.botd.domain;

public class BotdTrack {
    private final int globalIndex;
    private final String band;
    private final String track;
    private final BotdUser botdUser;

    public BotdTrack(int globalIndex, String band, String track, BotdUser botdUser) {
        this.globalIndex = globalIndex;
        this.band = band;
        this.track = track;
        this.botdUser = botdUser;
    }

    public int getGlobalIndex() {
        return globalIndex;
    }

    public String getBand() {
        return band;
    }

    public String getTrack() {
        return track;
    }

    public BotdUser getBotdUser() {
        return botdUser;
    }
}
