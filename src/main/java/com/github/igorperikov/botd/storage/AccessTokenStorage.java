package com.github.igorperikov.botd.storage;

public interface AccessTokenStorage {
    String get();

    void update(String accessToken);
}
