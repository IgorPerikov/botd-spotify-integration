package com.github.igorperikov.botd.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBasedInterProcessLock implements InterProcessLock {
    private static final Logger log = LoggerFactory.getLogger(FileBasedInterProcessLock.class);

    private static final Path LOCK_FILE_PATH = Path.of(
            "/",
            "Users",
            "igorperikov",
            "idea_projects",
            "botd_spotify_integration",
            "src",
            "main",
            "resources",
            "lock.txt"
    );

    private FileLock lock;

    public FileBasedInterProcessLock() {
        if (Files.notExists(LOCK_FILE_PATH)) {
            try {
                Files.createFile(LOCK_FILE_PATH);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        lock();
    }

    @Override
    public void close() {
        unlock();
    }

    private void lock() {
        File file = LOCK_FILE_PATH.toFile();
        try {
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
            this.lock = channel.tryLock(0, 1, false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain inter process lock");
        }
        if (this.lock == null) {
            throw new RuntimeException("Inter process lock already taken");
        }
    }

    private void unlock() {
        try {
            lock.release();
        } catch (IOException e) {
            log.error("Failed to release inter process lock", e);
        }
    }
}
