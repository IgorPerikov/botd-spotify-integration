package com.github.igorperikov.botd.accuracy;

import com.github.igorperikov.botd.entity.BotdTrack;
import com.github.igorperikov.botd.entity.SpotifyEntity;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpotifyEntityDistanceQualityPredicate implements Predicate<SpotifyEntity> {
    private static final Logger log = LoggerFactory.getLogger(SpotifyEntityDistanceQualityPredicate.class);

    private final String target;
    private final int maxAllowedDistance;

    public SpotifyEntityDistanceQualityPredicate(BotdTrack botdTrack) {
        this.target = botdTrack.getFullName();
        this.maxAllowedDistance = Math.max(3, (int) (target.length() * 0.35));
    }

    @Override
    public boolean test(SpotifyEntity track) {
        boolean isSimilar = Arrays.stream(track.getArtists())
                .map(artist -> artist.getName() + " " + track.getName())
                .anyMatch(name -> DistanceCalculationUtils.minPossibleDistance(name, target) <= maxAllowedDistance);
        log.info("'{}' and '{}' considered {}", target, convertToString(track), isSimilar ? "similar" : "not similar");
        return isSimilar;
    }

    private String convertToString(SpotifyEntity entity) {
        return Arrays.stream(entity.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(",")) +
                " " +
                entity.getName();
    }
}
