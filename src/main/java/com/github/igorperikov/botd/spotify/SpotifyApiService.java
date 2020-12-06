package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.accuracy.AccuracyService;
import com.github.igorperikov.botd.accuracy.DistanceCalculationUtils;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Authorization url
 * https://accounts.spotify.com/authorize?client_id=6624e196221e489a87490750cfa34354&response_type=code&redirect_uri
 * =https%3A%2F%2Fthis-is-my-website.io&scope=playlist-modify-public
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
    private final AccuracyService accuracyService;

    public SpotifyApiService(
            SongCache songCache,
            RefreshTokenStorage refreshTokenStorage,
            AccuracyService accuracyService
    ) {
        this.songCache = songCache;
        this.accuracyService = accuracyService;

        String storedRefreshToken = refreshTokenStorage.get();
        log.info("Found refresh token, is blank='{}'", StringUtils.isBlank(storedRefreshToken));
        AuthorizationCodeCredentials newTokens = getNewTokens(storedRefreshToken);
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
        List<SpotifyEntity> cachedSongs = songCache.lookup(botdTrack);
        List<SpotifyEntity> songsToAdd;
        if (cachedSongs.isEmpty()) {
            songsToAdd = findSongs(botdTrack);
        } else {
            songsToAdd = cachedSongs;
        }

        for (SpotifyEntity song : songsToAdd) {
            // TODO: we can add all at once
            addToPlaylist(song);
        }
        return !songsToAdd.isEmpty();
    }

    private List<SpotifyEntity> findSongs(BotdTrack botdTrack) {
        List<SpotifyEntity> candidates = findCandidates(botdTrack);
        if (candidates.size() == 0) {
            log.error("{} not found on spotify", botdTrack);
            return Collections.emptyList();
        }
        return accuracyService.findBest(botdTrack, candidates)
                .map(
                        spotifyEntity -> {
                            if (spotifyEntity.isTrack()) {
                                return Collections.singletonList(spotifyEntity);
                            } else {
                                return getTracksOfAlbum(spotifyEntity);
                            }
                        }
                ).orElse(Collections.emptyList());
    }

    private List<SpotifyEntity> findCandidates(BotdTrack botdTrack) {
        if (botdTrack.isAlbum()) {
            return Arrays.stream(findAlbumCandidates(botdTrack.getFullName()))
                    .map(SpotifyEntity::fromAlbum)
                    .collect(Collectors.toList());
        } else {
            return Arrays.stream(findSongCandidates(botdTrack.getFullName()))
                    .map(SpotifyEntity::fromTrack)
                    .collect(Collectors.toList());
        }
    }

    private Track[] findSongCandidates(String query) {
        Track[] foundTracks = findSongs(query);
        if (foundTracks.length == 0) {
            foundTracks = findSongs(DistanceCalculationUtils.removeParenthesesContent(query));
            if (foundTracks.length == 0) {
                return new Track[0];
            }
        }
        return foundTracks;
    }

    private Track[] findSongs(String query) {
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

    private List<SpotifyEntity> getTracksOfAlbum(SpotifyEntity spotifyEntity) {
        try {
            return Arrays.stream(
                    spotifyApi.getAlbumsTracks(spotifyEntity.getId())
                            .market(CountryCode.RU)
                            .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                            .build()
                            .execute()
                            .getItems())
                    .map(SpotifyEntity::fromTrack)
                    .collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void addToPlaylist(SpotifyEntity song) {
        try {
            // TODO: batch optimization
            spotifyApi.addItemsToPlaylist(PLAYLIST_ID, new String[]{song.getId()}).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthorizationCodeCredentials getNewTokens(String refreshToken) {
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
