package com.github.igorperikov.botd.accuracy;

import com.github.igorperikov.botd.data.domain.BotdTrack;
import com.github.igorperikov.botd.spotify.Album;
import com.github.igorperikov.botd.spotify.Song;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
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
                .filter(new TrackDistanceQualityPredicate(botdTrack))
                .min(trackComparatorByDistance(botdTrack));
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

    public Optional<Album> findBest(BotdTrack botdTrack, AlbumSimplified[] albums) {
        Optional<AlbumSimplified> mostAccurate = Arrays.stream(albums)
                .filter(new AlbumDistanceQualityPredicate(botdTrack))
                .min(albumComparatorByDistance(botdTrack));
        if (mostAccurate.isEmpty()) {
            log.error(
                    "Didn't find album close enough to={}, found={}",
                    botdTrack,
                    albumsToString(albums)
            );
            return Optional.empty();
        }
        AlbumSimplified result = mostAccurate.get();
        log.info(
                "Resolved album#{} '{}' as '{}', uri='{}'",
                botdTrack.getGlobalIndex(),
                botdTrack.getSimpleName(),
                result.getArtists()[0].getName() + " " + result.getName(),
                result.getUri()
        );
        return Optional.of(new Album(result.getId()));
    }

    private Comparator<Track> trackComparatorByDistance(BotdTrack botdTrack) {
        return Comparator.comparingInt(track ->
                Arrays.stream(track.getArtists())
                        .map(artist -> artist.getName() + " " + track.getName())
                        .map(trackName -> DistanceCalculator.distance(trackName, botdTrack.getSimpleName()))
                        .min(Integer::compareTo)
                        .get()
        );
    }

    private Comparator<AlbumSimplified> albumComparatorByDistance(BotdTrack botdTrack) {
        return Comparator.comparingInt(album ->
                Arrays.stream(album.getArtists())
                        .map(artist -> artist.getName() + " " + album.getName())
                        .map(trackName -> DistanceCalculator.distance(trackName, botdTrack.getSimpleName()))
                        .min(Integer::compareTo)
                        .get()
        );
    }

    private static String albumsToString(AlbumSimplified[] albums) {
        return Arrays.stream(albums)
                .map(TrackAccuracyService::albumToString)
                .collect(Collectors.joining(" ; ", "[", "]"));
    }

    private static String albumToString(AlbumSimplified album) {
        String artists = Arrays.stream(album.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(","));
        return artists + "-" + album.getName();
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
