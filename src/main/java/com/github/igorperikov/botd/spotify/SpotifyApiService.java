package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.domain.BotdTrack;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Authorization url
 * https://accounts.spotify.com/authorize?client_id=6624e196221e489a87490750cfa34354&response_type=code&redirect_uri=https%3A%2F%2Fthis-is-my-website.io&scope=playlist-modify-public
 */
public class SpotifyApiService {
    private static final Logger log = LoggerFactory.getLogger(SpotifyApiService.class);

    private static final String PLAYLIST_ID = "55RwDsEAaMLq4iVFnRrxFc";
    private static final int NUMBER_OF_TRACKS_TO_SEARCH_FOR = 10;

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

    public SpotifyApiService(SongCache songCache, RefreshTokenStorage refreshTokenStorage) {
        this.songCache = songCache;

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
     * @return true if track was found and added
     */
    public boolean add(BotdTrack botdTrack) {
        Optional<Song> cachedSong = songCache.lookup(botdTrack);
        Optional<Song> songToAdd = cachedSong.or(() -> findSong(botdTrack));

        if (songToAdd.isPresent()) {
            addToPlaylist(songToAdd.get());
            return true;
        } else {
            return false;
        }
    }

    private Optional<Song> findSong(BotdTrack botdTrack) {
        Track[] foundTracks = findSongCandidates(botdTrack);
        if (foundTracks.length == 0) {
            log.error("Track {} not found on spotify", botdTrack);
            return Optional.empty();
        }

        Optional<Track> mostAccurate = Arrays.stream(foundTracks)
                .filter(new DistanceLessThanTargetPredicate(botdTrack))
                .min(new SpotifyTrackComparator(botdTrack));
        if (mostAccurate.isEmpty()) {
            log.error(
                    "Didn't find track which is close enough to {}, available tracks were {}",
                    botdTrack,
                    tracksToString(foundTracks)
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

    private Track[] findSongCandidates(BotdTrack botdTrack) {
        try {
            return spotifyApi.searchTracks(botdTrack.getSimpleName())
                    .market(CountryCode.RU)
                    .limit(NUMBER_OF_TRACKS_TO_SEARCH_FOR)
                    .build()
                    .execute()
                    .getItems();
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

    private String tracksToString(Track[] tracks) {
        return Arrays.stream(tracks)
                .map(this::trackToString)
                .collect(Collectors.joining(" ; ", "[", "]"));
    }

    private String trackToString(Track track) {
        String artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(","));
        return artists + "-" + track.getAlbum().getName() + "-" + track.getName();
    }
}
