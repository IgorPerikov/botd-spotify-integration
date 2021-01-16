package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.storage.AccessTokenStorage;
import com.github.igorperikov.botd.storage.RefreshTokenStorage;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class SpotifyApiFactory {
    private static final Logger log = LoggerFactory.getLogger(SpotifyApiFactory.class);

    private static final String CLIENT_ID = Objects.requireNonNull(
            System.getenv("BOTD_SPOTIFY_CLIENT_ID"),
            "provide BOTD_SPOTIFY_CLIENT_ID envvar"
    );
    private static final String CLIENT_SECRET = Objects.requireNonNull(
            System.getenv("BOTD_SPOTIFY_CLIENT_SECRET"),
            "provide BOTD_SPOTIFY_CLIENT_SECRET envvar"
    );

    private final RefreshTokenStorage refreshTokenStorage;
    private final AccessTokenStorage accessTokenStorage;

    public SpotifyApiFactory(RefreshTokenStorage refreshTokenStorage, AccessTokenStorage accessTokenStorage) {
        this.refreshTokenStorage = refreshTokenStorage;
        this.accessTokenStorage = accessTokenStorage;
    }

    public SpotifyApi create() {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .build();

        String storedRefreshToken = refreshTokenStorage.get();

        Optional<AuthorizationCodeCredentials> newTokens = tryUpdateTokens(spotifyApi, storedRefreshToken);
        String accessToken;
        if (newTokens.isPresent()) {
            if (newTokens.get().getRefreshToken() != null) {
                log.info("Got new refresh token from accounts service, updating");
                refreshTokenStorage.update(newTokens.get().getRefreshToken());
            }
            accessToken = newTokens.get().getAccessToken();
            accessTokenStorage.update(accessToken);
        } else {
            accessToken = accessTokenStorage.get();
        }
        spotifyApi.setAccessToken(accessToken);
        return spotifyApi;
    }

    private Optional<AuthorizationCodeCredentials> tryUpdateTokens(SpotifyApi spotifyApi, String refreshToken) {
        try {
            return Optional.of(
                    spotifyApi.authorizationCodeRefresh()
                            .grant_type("refresh_token")
                            .refresh_token(refreshToken)
                            .build()
                            .execute()
            );
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Failed to update auth tokens", e);
            return Optional.empty();
        }
    }
}
