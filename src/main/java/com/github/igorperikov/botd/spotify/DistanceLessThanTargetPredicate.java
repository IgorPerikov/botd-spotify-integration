package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.domain.BotdTrack;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DistanceLessThanTargetPredicate implements Predicate<Track> {
    private static final Logger log = LoggerFactory.getLogger(DistanceLessThanTargetPredicate.class);

    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    private final String target;
    private final int maxAllowedDistance;

    public DistanceLessThanTargetPredicate(BotdTrack botdTrack) {
        this.target = botdTrack.getSimpleName();
        this.maxAllowedDistance = Math.max(3, (int) (target.length() * 0.25));
    }

    @Override
    public boolean test(Track track) {
        boolean isSimilar = Arrays.stream(track.getArtists())
                .map(artist -> artist.getName() + " " + track.getName())
                .anyMatch(name -> levenshteinDistance.apply(name, target) <= maxAllowedDistance);
        log.info("'{}' and '{}' considered as '{}'", convertToString(track), target, isSimilar ? "similar" : "not similar");
        return isSimilar;
    }

    private String convertToString(Track track) {
        return Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(",")) +
                ":" +
                track.getName();
    }
}
