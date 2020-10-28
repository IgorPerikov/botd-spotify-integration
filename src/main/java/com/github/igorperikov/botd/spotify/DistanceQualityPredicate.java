package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.domain.BotdTrack;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DistanceQualityPredicate implements Predicate<Track> {
    private static final Logger log = LoggerFactory.getLogger(DistanceQualityPredicate.class);

    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    private final String target;
    private final int maxAllowedDistance;

    public DistanceQualityPredicate(BotdTrack botdTrack) {
        this.target = botdTrack.getSimpleName();
        this.maxAllowedDistance = Math.max(3, (int) (target.length() * 0.25));
    }

    @Override
    public boolean test(Track track) {
        boolean isSimilar = Arrays.stream(track.getArtists())
                .map(artist -> artist.getName() + " " + track.getName())
                .anyMatch(name -> minPossibleDistance(name, target) <= maxAllowedDistance);
        log.info("'{}' and '{}' considered as '{}'", target, convertToString(track), isSimilar ? "similar" : "not similar");
        return isSimilar;
    }

    private int minPossibleDistance(String first, String second) {
        return Math.min(
                Math.min(
                        distance(first, second),
                        distance(removeParenthesesContent(first), second)
                ),
                Math.min(
                        distance(first, removeParenthesesContent(second)),
                        distance(removeParenthesesContent(first), removeParenthesesContent(second))
                )
        );
    }

    private String convertToString(Track track) {
        return Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(",")) +
                " " +
                track.getName();
    }

    private int distance(String first, String second) {
        return levenshteinDistance.apply(first.toLowerCase(), second.toLowerCase());
    }

    private static String removeParenthesesContent(String str) {
        if (str.contains("(") && str.contains(")")) {
            return StringUtils.normalizeSpace(str.replaceAll("(\\(.*?\\))", ""));
        } else {
            return str;
        }
    }
}
