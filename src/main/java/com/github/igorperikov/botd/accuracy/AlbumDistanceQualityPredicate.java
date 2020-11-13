package com.github.igorperikov.botd.accuracy;

import com.github.igorperikov.botd.data.domain.BotdTrack;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AlbumDistanceQualityPredicate implements Predicate<AlbumSimplified> {
    private static final Logger log = LoggerFactory.getLogger(TrackDistanceQualityPredicate.class);

    private final String target;
    private final int maxAllowedDistance;

    public AlbumDistanceQualityPredicate(BotdTrack botdTrack) {
        this.target = botdTrack.getSimpleName();
        this.maxAllowedDistance = Math.max(3, (int) (target.length() * 0.2));
    }

    @Override
    public boolean test(AlbumSimplified track) {
        boolean isSimilar = Arrays.stream(track.getArtists())
                .map(artist -> artist.getName() + " " + track.getName())
                .anyMatch(name -> DistanceCalculator.minPossibleDistance(name, target) <= maxAllowedDistance);
        log.info("'{}' and '{}' considered as '{}'", target, convertToString(track), isSimilar ? "similar" : "not similar");
        return isSimilar;
    }

    private String convertToString(AlbumSimplified album) {
        return Arrays.stream(album.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(",")) +
                " " +
                album.getName();
    }
}
