package com.github.igorperikov.botd;

import com.github.igorperikov.botd.accuracy.AccuracyService;
import com.github.igorperikov.botd.cache.ResourceFileSongCache;
import com.github.igorperikov.botd.entity.BotdTrack;
import com.github.igorperikov.botd.execution.AppExecutionContext;
import com.github.igorperikov.botd.execution.FileBasedInterProcessLock;
import com.github.igorperikov.botd.parser.BotdDataExtractor;
import com.github.igorperikov.botd.parser.SpreadsheetsFactory;
import com.github.igorperikov.botd.restart.CleanupService;
import com.github.igorperikov.botd.restart.Md5RestartService;
import com.github.igorperikov.botd.restart.ResourceFileMd5Storage;
import com.github.igorperikov.botd.spotify.SpotifyApiFactory;
import com.github.igorperikov.botd.spotify.SpotifyApiService;
import com.github.igorperikov.botd.storage.ResourceFileAccessTokenStorage;
import com.github.igorperikov.botd.storage.ResourceFileProgressStorage;
import com.github.igorperikov.botd.storage.ResourceFileRefreshTokenStorage;
import com.github.igorperikov.botd.telegram.TelegramMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// todo: unify logging/telegram behind single facade
// todo tests/decoupling
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        var telegramMessageSender = new TelegramMessageSender();

        try (
                var context = new AppExecutionContext(telegramMessageSender);
                var ignored = new FileBasedInterProcessLock()
        ) {
            var spotifyApiFactory = new SpotifyApiFactory(
                    new ResourceFileRefreshTokenStorage(),
                    new ResourceFileAccessTokenStorage()
            );
            var spotifyApiService = new SpotifyApiService(
                    spotifyApiFactory.create(),
                    new ResourceFileSongCache(),
                    new AccuracyService(),
                    telegramMessageSender
            );

            var progressStorage = new ResourceFileProgressStorage();
            var sheets = SpreadsheetsFactory.create();
            var extractor = new BotdDataExtractor(sheets);
            var botdData = extractor.extract();
            var md5Storage = new ResourceFileMd5Storage();

            var restartRequired = new Md5RestartService(md5Storage, progressStorage).restartRequired(botdData);
            if (restartRequired) {
                context.registerRestart();
                new CleanupService(spotifyApiService, progressStorage).cleanup();
            }

            for (BotdTrack botdTrack : progressStorage.getAllUnprocessed(botdData)) {
                log.info("Start processing {}", botdTrack);
                boolean added = spotifyApiService.add(botdTrack);
                if (added) {
                    context.registerNewTrackAddition();
                }
                progressStorage.markAsProcessed(botdTrack);
                log.info("Finish processing {}", botdTrack);
            }
            md5Storage.write(progressStorage.getMd5OfAllProcessed(botdData));
        } catch (Exception e) {
            log.error("Execution failed:", e);
        }
    }
}
