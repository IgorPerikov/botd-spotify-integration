package com.github.igorperikov.botd.execution;

import com.github.igorperikov.botd.telegram.TelegramMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppExecutionContext implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(AppExecutionContext.class);

    private final TelegramMessageSender telegramMessageSender;

    private int tracksBefore = 0;
    private int tracksAdded = 0;
    private boolean restartRequired = false;

    public AppExecutionContext(TelegramMessageSender telegramMessageSender) {
        this.telegramMessageSender = telegramMessageSender;
    }

    @Override
    public void close() {
        log.info("Start closing context with restart={}, added tracks={}", restartRequired, tracksAdded);
        if (restartRequired) {
            telegramMessageSender.sendAllChat(String.format(
                    "Playlist is available again, number of tracks %d -> %d",
                    tracksBefore,
                    tracksAdded
            ));
        } else if (tracksAdded != 0) {
            telegramMessageSender.sendAllChat(
                    String.format(
                            "%d %s has been added to playlist",
                            tracksAdded,
                            tracksAdded == 1 ? "track" : "tracks"
                    )
            );
        }
    }

    public void registerRestart(int tracksBefore) {
        telegramMessageSender.sendAllChat("Playlist destruction initiated");
        this.restartRequired = true;
        this.tracksBefore = tracksBefore;
    }

    public void registerNewTrackAddition(int increment) {
        this.tracksAdded += increment;
    }
}
