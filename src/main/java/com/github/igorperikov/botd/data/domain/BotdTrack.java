package com.github.igorperikov.botd.data.domain;

public class BotdTrack {
    private final int globalIndex;
    private final String band;
    private final String name;
    private final boolean isAlbum;
    private final BotdUser botdUser;

    public BotdTrack(int globalIndex, String band, String name, boolean isAlbum, BotdUser botdUser) {
        this.globalIndex = globalIndex;
        this.band = band;
        this.name = name;
        this.isAlbum = isAlbum;
        this.botdUser = botdUser;
    }

    public int getGlobalIndex() {
        return globalIndex;
    }

    public String getBand() {
        return band;
    }

    public String getName() {
        return name;
    }

    public boolean isAlbum() {
        return isAlbum;
    }

    public BotdUser getBotdUser() {
        return botdUser;
    }

    public String getSimpleName() {
        return getBand() + " " + getName();
    }

    @Override
    public String toString() {
        return "Track{" +
                "band=" + band +
                ", track=" + name +
                ", album=" + isAlbum +
                ", index=" + globalIndex +
                '}';
    }
}
