package com.github.igorperikov.botd.accuracy;

import com.github.igorperikov.botd.data.domain.BotdTrack;
import com.github.igorperikov.botd.spotify.Song;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrackAccuracyService {
    private static final Logger log = LoggerFactory.getLogger(TrackAccuracyService.class);

    public Optional<Song> findBest(BotdTrack botdTrack, Track[] tracks) {
        Optional<Track> mostAccurate = Arrays.stream(tracks)
                .filter(new DistanceQualityPredicate(botdTrack))
                .min(comparatorByDistance(botdTrack));
        if (mostAccurate.isEmpty()) {
            log.error(
                    "Didn't find track close enough to={}, found={}",
                    botdTrack,
                    tracksToString(tracks)
            );
            return Optional.empty();
        }
        Track result = mostAccurate.get();
        String uri = result.getUri();
        log.info(
                "Resolved track#{} '{}' as '{}', uri='{}'",
                botdTrack.getGlobalIndex(),
                botdTrack.getSimpleName(),
                result.getArtists()[0].getName() + " " + result.getName(),
                uri
        );
        return Optional.of(new Song(uri));
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

    private static String tracksToString(Track[] tracks) {
        return Arrays.stream(tracks)
                .map(TrackAccuracyService::trackToString)
                .collect(Collectors.joining(" ; ", "[", "]"));
    }

    private static String trackToString(Track track) {
        String artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(","));
        return artists + "-" + track.getAlbum().getName() + "-" + track.getName();
    }
}
