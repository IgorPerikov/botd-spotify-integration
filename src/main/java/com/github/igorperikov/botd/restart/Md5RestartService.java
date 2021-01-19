package com.github.igorperikov.botd.restart;

import com.github.igorperikov.botd.entity.BotdData;
import com.github.igorperikov.botd.storage.ProgressStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Md5RestartService implements RestartService {
    private static final Logger log = LoggerFactory.getLogger(Md5RestartService.class);

    private final Md5Storage md5Storage;
    private final ProgressStorage progressStorage;

    public Md5RestartService(Md5Storage md5Storage, ProgressStorage progressStorage) {
        this.md5Storage = md5Storage;
        this.progressStorage = progressStorage;
    }

    @Override
    public boolean restartRequired(BotdData botdData) {
        String expected = md5Storage.read();
        String actual = progressStorage.getMd5OfAllProcessed(botdData);

        boolean restartRequired = !actual.equals(expected);
        log.info("Restart required='{}', actual='{}', expected='{}'", restartRequired, actual, expected);
        return restartRequired;
    }
}
