package com.github.igorperikov.botd;

import com.github.igorperikov.botd.telegram.TelegramMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppExecutionContext implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(AppExecutionContext.class);

    private static final TelegramMessageSender MESSAGE_SENDER = new TelegramMessageSender();

    private int newlyAddedTracks = 0;
    private boolean restartRequired = false;

    @Override
    public void close() {
        log.info("Start closing context with restart={}, added tracks={}", restartRequired, newlyAddedTracks);
        if (restartRequired) {
            MESSAGE_SENDER.send("Playlist is available again, you can leave your shelter");
        } else if (newlyAddedTracks != 0) {
            MESSAGE_SENDER.send(
                    String.format(
                            "%d %s has been added to playlist",
                            newlyAddedTracks,
                            newlyAddedTracks == 1 ? "track" : "tracks"
                    )
            );
        }
    }

    public void registerRestart() {
        MESSAGE_SENDER.send("Playlist destruction initiated, find a shelter immediately");
        restartRequired = true;
    }

    public void registerNewTrackAddition() {
        newlyAddedTracks++;
    }
}
