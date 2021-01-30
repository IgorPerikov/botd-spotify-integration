package com.github.igorperikov.botd.restart;

import com.github.igorperikov.botd.storage.ResourceFileStorage;

public class ResourceFileMd5Storage extends ResourceFileStorage implements Md5Storage {
    public ResourceFileMd5Storage() {
        super("md5.txt");
        createIfNotExists();
    }

    @Override
    public void write(String md5) {
        replaceWithNewContent(md5);
    }

    @Override
    public String read() {
        return readStringOrEmpty();
    }
}
