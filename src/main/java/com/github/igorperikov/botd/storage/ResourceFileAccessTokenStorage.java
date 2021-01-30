package com.github.igorperikov.botd.storage;

public class ResourceFileAccessTokenStorage extends ResourceFileStorage implements AccessTokenStorage {
    public ResourceFileAccessTokenStorage() {
        super("access_token.txt");
    }

    @Override
    public String get() {
        return readStringOrEmpty();
    }

    @Override
    public void update(String accessToken) {
        replaceWithNewContent(accessToken);
    }
}
