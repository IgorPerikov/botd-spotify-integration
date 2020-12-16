package com.github.igorperikov.botd;

import com.github.igorperikov.botd.accuracy.AccuracyService;
import com.github.igorperikov.botd.entity.BotdStage;
import com.github.igorperikov.botd.parser.BotdDataExtractor;
import com.github.igorperikov.botd.parser.SpreadsheetsFactory;
import com.github.igorperikov.botd.restart.CleanupService;
import com.github.igorperikov.botd.restart.RestartService;
import com.github.igorperikov.botd.spotify.SpotifyApiService;
import com.github.igorperikov.botd.storage.LocalFileProgressStorage;
import com.github.igorperikov.botd.storage.LocalFileRefreshTokenStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        var refreshTokenStorage = new LocalFileRefreshTokenStorage();
        var trackAccuracyService = new AccuracyService();
        var spotifyApiService = new SpotifyApiService(
                botdTrack -> Collections.emptyList(),
                refreshTokenStorage,
                trackAccuracyService
        );

        var progressStorage = new LocalFileProgressStorage();
        var sheets = SpreadsheetsFactory.create();
        var extractor = new BotdDataExtractor(sheets);
        var botdData = extractor.extract();

        RestartService restartService = () -> false;
        if (restartService.restartRequired()) {
            new CleanupService(spotifyApiService, progressStorage).cleanup();
        }

        botdData.getBotdStages().stream()
                .map(BotdStage::getTracks)
                .flatMap(Collection::stream)
                .filter(botdTrack -> !progressStorage.isProcessed(botdTrack))
                .forEach(botdTrack -> {
                    log.info("Start processing {}", botdTrack);
                    boolean added = spotifyApiService.add(botdTrack);
                    progressStorage.markAsProcessed(botdTrack);
                    log.info("Finish processing {}", botdTrack);
                });
    }
}
