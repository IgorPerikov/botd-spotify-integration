package com.github.igorperikov.botd.execution;

import com.github.igorperikov.botd.telegram.TelegramMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppExecutionContext implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(AppExecutionContext.class);

    private final TelegramMessageSender telegramMessageSender;

    private int newlyAddedTracks = 0;
    private boolean restartRequired = false;

    public AppExecutionContext(TelegramMessageSender telegramMessageSender) {
        this.telegramMessageSender = telegramMessageSender;
    }

    @Override
    public void close() {
        log.info("Start closing context with restart={}, added tracks={}", restartRequired, newlyAddedTracks);
        if (restartRequired) {
            telegramMessageSender.sendAllChat("Playlist is available again, you can leave your shelter");
        } else if (newlyAddedTracks != 0) {
            telegramMessageSender.sendAllChat(
                    String.format(
                            "%d %s has been added to playlist",
                            newlyAddedTracks,
                            newlyAddedTracks == 1 ? "track" : "tracks"
                    )
            );
        }
    }

    public void registerRestart() {
        telegramMessageSender.sendAllChat("Playlist destruction initiated, find a shelter immediately");
        restartRequired = true;
    }

    public void registerNewTrackAddition() {
        newlyAddedTracks++;
    }
}
