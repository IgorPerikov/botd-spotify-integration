package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.accuracy.DistanceCalculator;
import com.github.igorperikov.botd.accuracy.TrackAccuracyService;
import com.github.igorperikov.botd.data.domain.BotdTrack;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authorization url
 * https://accounts.spotify.com/authorize?client_id=6624e196221e489a87490750cfa34354&response_type=code&redirect_uri=https%3A%2F%2Fthis-is-my-website.io&scope=playlist-modify-public
 */
public class SpotifyApiService {
    private static final Logger log = LoggerFactory.getLogger(SpotifyApiService.class);

    private static final String PLAYLIST_ID = "55RwDsEAaMLq4iVFnRrxFc";
    private static final int NUMBER_OF_ITEMS_TO_SEARCH_FOR = 20;

    private static final String CLIENT_ID = Objects.requireNonNull(
            System.getenv("BOTD_SPOTIFY_CLIENT_ID"),
            "provide BOTD_SPOTIFY_CLIENT_ID envvar"
    );
    private static final String CLIENT_SECRET = Objects.requireNonNull(
            System.getenv("BOTD_SPOTIFY_CLIENT_SECRET"),
            "provide BOTD_SPOTIFY_CLIENT_SECRET envvar"
    );

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
            .build();

    private final SongCache songCache;
    private final TrackAccuracyService trackAccuracyService;

    public SpotifyApiService(
            SongCache songCache,
            RefreshTokenStorage refreshTokenStorage,
            TrackAccuracyService trackAccuracyService
    ) {
        this.songCache = songCache;
        this.trackAccuracyService = trackAccuracyService;

        String storedRefreshToken = refreshTokenStorage.get();
        log.info("Found refresh token, is blank='{}'", StringUtils.isBlank(storedRefreshToken));
        AuthorizationCodeCredentials newTokens = updateTokens(storedRefreshToken);
        if (newTokens.getRefreshToken() != null) {
            log.info("Got new refresh token from accounts service, updating");
            refreshTokenStorage.update(newTokens.getRefreshToken());
        }
        spotifyApi.setAccessToken(newTokens.getAccessToken());
    }

    /**
     * @return true if something was found and added
     */
    public boolean add(BotdTrack botdTrack) {
        List<Song> cachedSongs = songCache.lookup(botdTrack);
        List<Song> songsToAdd;
        if (cachedSongs.isEmpty()) {
            songsToAdd = findSongs(botdTrack);
        } else {
            songsToAdd = cachedSongs;
        }

        for (Song song : songsToAdd) {
            addToPlaylist(song);
        }
        return !songsToAdd.isEmpty();
    }

    private List<Song> findSongs(BotdTrack botdTrack) {
        if (botdTrack.isAlbum()) {
            AlbumSimplified[] albumCandidates = findAlbumCandidates(botdTrack.getSimpleName());
            if (albumCandidates.length == 0) {
                log.error("Album {} not found on spotify", botdTrack);
                return Collections.emptyList();
            }
            Optional<Album> best = trackAccuracyService.findBest(botdTrack, albumCandidates);
            return best.map(this::getTracksOfAlbum).orElse(Collections.emptyList());
        } else {
            Track[] foundTracks = findSongs(botdTrack.getSimpleName());
            if (foundTracks.length == 0) {
                log.error("Track {} not found on spotify", botdTrack);
                return Collections.emptyList();
            }
            return trackAccuracyService.findBest(botdTrack, foundTracks)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }
    }

    private Track[] findSongs(String name) {
        Track[] foundTracks = findSongCandidates(name);
        if (foundTracks.length == 0) {
            foundTracks = findSongCandidates(DistanceCalculator.removeParenthesesContent(name));
            if (foundTracks.length == 0) {
                return new Track[0];
            }
        }
        return foundTracks;
    }

    private Track[] findSongCandidates(String query) {
        try {
            return spotifyApi.searchTracks(query)
                    .market(CountryCode.RU)
                    .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                    .build()
                    .execute()
                    .getItems();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private AlbumSimplified[] findAlbumCandidates(String query) {
        try {
            return spotifyApi.searchAlbums(query)
                    .market(CountryCode.RU)
                    .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                    .build()
                    .execute()
                    .getItems();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Song> getTracksOfAlbum(Album album) {
        try {
            return Arrays.stream(
                    spotifyApi.getAlbumsTracks(album.getId())
                            .market(CountryCode.RU)
                            .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                            .build()
                            .execute()
                            .getItems())
                    .map(trackSimplified -> new Song(trackSimplified.getUri()))
                    .collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void addToPlaylist(Song song) {
        try {
            spotifyApi.addItemsToPlaylist(PLAYLIST_ID, new String[]{song.getSpotifyURI()}).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthorizationCodeCredentials updateTokens(String refreshToken) {
        try {
            return spotifyApi.authorizationCodeRefresh()
                    .grant_type("refresh_token")
                    .refresh_token(refreshToken)
                    .build()
                    .execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
