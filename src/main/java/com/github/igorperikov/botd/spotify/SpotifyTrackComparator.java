package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.domain.BotdTrack;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Smaller means more accurate
 */
public class SpotifyTrackComparator implements Comparator<Track> {
    private final String trackName;
    private final String bandName;

    public SpotifyTrackComparator(BotdTrack botdTrack) {
        this.trackName = botdTrack.getTrack();
        this.bandName = botdTrack.getBand();
    }

    @Override
    public int compare(Track track1, Track track2) {
        return Integer.compare(countScore(track1), countScore(track2));
    }

    private int countScore(Track track) {
        int score = 0;
        if (!trackName.equalsIgnoreCase(track.getName())) {
            score++;
        }
        Set<String> artists = Arrays.stream(track.getArtists())
                .map(ArtistSimplified::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (!artists.contains(bandName.toLowerCase())) {
            score++;
        }
        return score;
    }
}
