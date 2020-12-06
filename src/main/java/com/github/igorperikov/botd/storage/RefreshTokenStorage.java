package com.github.igorperikov.botd.storage;

public interface RefreshTokenStorage {
    String get();

    void update(String refreshToken);
}
