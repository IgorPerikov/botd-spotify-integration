package com.github.igorperikov.botd.restart;

public interface Md5Storage {
    void write(String md5);

    String read();
}
