package com.github.igorperikov.botd;

import com.github.igorperikov.botd.domain.BotdData;
import com.github.igorperikov.botd.domain.BotdStage;
import com.github.igorperikov.botd.progress.LocalFileProgressStorage;
import com.github.igorperikov.botd.spotify.LocalFileRefreshTokenStorage;
import com.github.igorperikov.botd.spotify.SpotifyApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        var sheets = SpreadsheetsInit.getService();
        var extractor = new BotdDataExtractor(sheets);
        var progressStorage = new LocalFileProgressStorage();
        var refreshTokenStorage = new LocalFileRefreshTokenStorage();
        var spotifyApiService = new SpotifyApiService(botdTrack -> Optional.empty(), refreshTokenStorage);

        BotdData botdData = extractor.extract();
        botdData.getBotdStages().stream()
                .map(BotdStage::getTracks)
                .flatMap(Collection::stream)
                .filter(botdTrack -> !progressStorage.isProcessed(botdTrack))
                .forEach(botdTrack -> {
                    log.info("Start processing {}", botdTrack);
                    spotifyApiService.add(botdTrack);
                    progressStorage.markAsProcessed(botdTrack);
                    log.info("Finish processing {}", botdTrack);
                });
    }
}
