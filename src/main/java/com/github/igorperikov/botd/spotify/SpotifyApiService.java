package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.domain.BotdTrack;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Authorization url
 * https://accounts.spotify.com/authorize?client_id=6624e196221e489a87490750cfa34354&response_type=code&redirect_uri=https%3A%2F%2Fthis-is-my-website.io&scope=playlist-modify-public
 */
public class SpotifyApiService {
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
        AuthorizationCodeCredentials newTokens = getTokens(storedRefreshToken);
        if (newTokens.getRefreshToken() != null) {
            refreshTokenStorage.update(newTokens.getRefreshToken());
        }
        spotifyApi.setAccessToken(newTokens.getAccessToken());
    }

    public void add(BotdTrack botdTrack) {
        Optional<Song> cachedSong = songCache.lookup(botdTrack);
        Song songToAdd = cachedSong.orElseGet(() -> findSong(botdTrack));
        addToPlaylist(songToAdd);
    }

    private Song findSong(BotdTrack botdTrack) {
        // TODO:
    }

    private void addToPlaylist(Song song) {
        // TODO:
    }

    private AuthorizationCodeCredentials getTokens(String refreshToken) {
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
