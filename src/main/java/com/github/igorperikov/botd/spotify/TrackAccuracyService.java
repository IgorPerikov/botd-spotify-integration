package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.domain.BotdTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class TrackAccuracyService {
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    public Optional<Track> findBest(BotdTrack botdTrack, Track[] tracks) {
        return Arrays.stream(tracks)
                .filter(new DistanceQualityPredicate(botdTrack))
                .min(comparatorByDistance(botdTrack));
    }

    private Comparator<Track> comparatorByDistance(BotdTrack botdTrack) {
        return Comparator.comparingInt(track ->
                distance( // TODO: min distance across all artists ?
                        botdTrack.getSimpleName(),
                        track.getArtists()[0].getName() + " " + track.getName()
                )
        );
    }

    private int distance(String first, String second) {
        return levenshteinDistance.apply(first.toLowerCase(), second.toLowerCase());
    }
}
