package com.github.igorperikov.botd.spotify;

public interface RefreshTokenStorage {
    String get();

    void update(String refreshToken);
}
