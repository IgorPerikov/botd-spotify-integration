package com.github.igorperikov.botd.accuracy;

import com.github.igorperikov.botd.data.domain.BotdTrack;
import com.github.igorperikov.botd.spotify.SpotifyEntity;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccuracyService {
    private static final Logger log = LoggerFactory.getLogger(AccuracyService.class);

    public Optional<SpotifyEntity> findBest(BotdTrack botdTrack, List<SpotifyEntity> candidates) {
        Optional<SpotifyEntity> mostAccurate = candidates.stream()
                .filter(new SpotifyEntityDistanceQualityPredicate(botdTrack))
                .min(spotifyEntityComparatorByDistance(botdTrack));
        if (mostAccurate.isEmpty()) {
            log.error(
                    "Didn't find anything close enough to={}, found={}",
                    botdTrack,
                    entitiesToString(candidates)
            );
            return Optional.empty();
        }
        SpotifyEntity result = mostAccurate.get();
        log.info(
                "Resolved {} as '{}', id='{}'",
                botdTrack,
                result.getArtists()[0].getName() + " " + result.getName(),
                result.getId()
        );
        return Optional.of(result);
    }

    private Comparator<SpotifyEntity> spotifyEntityComparatorByDistance(BotdTrack botdTrack) {
        return Comparator.comparingInt(entity ->
                Arrays.stream(entity.getArtists())
                        .map(artist -> artist.getName() + " " + entity.getName())
                        .map(trackName -> DistanceCalculationUtils.distance(trackName, botdTrack.getFullName()))
                        .min(Integer::compareTo)
                        .get()
        );
    }

    private static String entitiesToString(List<SpotifyEntity> entities) {
        return entities.stream()
                .map(AccuracyService::entityToString)
                .collect(Collectors.joining(" ; ", "[", "]"));
    }

    private static String entityToString(SpotifyEntity entity) {
        String artists = Arrays.stream(entity.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(","));
        return artists + "-" + entity.getName();
    }
}
