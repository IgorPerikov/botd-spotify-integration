package com.github.igorperikov.botd.accuracy;

import com.github.igorperikov.botd.data.domain.BotdTrack;
import com.wrapper.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class TrackAccuracyService {
    public Optional<Track> findBest(BotdTrack botdTrack, Track[] tracks) {
        return Arrays.stream(tracks)
                .filter(new DistanceQualityPredicate(botdTrack))
                .min(comparatorByDistance(botdTrack));
    }

    private Comparator<Track> comparatorByDistance(BotdTrack botdTrack) {
        return Comparator.comparingInt(track ->
                Arrays.stream(track.getArtists())
                        .map(artist -> artist.getName() + " " + track.getName())
                        .map(trackName -> DistanceCalculator.distance(trackName, botdTrack.getSimpleName()))
                        .min(Integer::compareTo)
                        .get()
        );
    }
}
