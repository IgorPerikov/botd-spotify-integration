package com.github.igorperikov.botd.storage;

public class ResourceFileRefreshTokenStorage extends ResourceFileStorage implements RefreshTokenStorage {
    public ResourceFileRefreshTokenStorage() {
        super("refresh_token.txt");
    }

    @Override
    public String get() {
        return readStringOrEmpty();
    }

    @Override
    public void update(String refreshToken) {
        replaceWithNewContent(refreshToken);
    }
}
