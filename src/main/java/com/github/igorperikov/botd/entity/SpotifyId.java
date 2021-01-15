package com.github.igorperikov.botd.entity;

public class SpotifyId {
    /**
     * uri for song, id for album
     */
    private final String id;

    public SpotifyId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
