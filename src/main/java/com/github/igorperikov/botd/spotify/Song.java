package com.github.igorperikov.botd.spotify;

public class Song {
    private final String spotifyURI;

    public Song(String spotifyURI) {
        this.spotifyURI = spotifyURI;
    }

    public String getSpotifyURI() {
        return spotifyURI;
    }
}
