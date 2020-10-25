package com.github.igorperikov.botd;

import com.github.igorperikov.botd.domain.BotdData;
import com.github.igorperikov.botd.domain.BotdStage;
import com.github.igorperikov.botd.progress.LocalFileProgressStorage;
import com.github.igorperikov.botd.progress.ProgressStorage;
import com.google.api.services.sheets.v4.Sheets;

import java.util.Collection;

public class Application {
    public static void main(String[] args) {
        Sheets sheets = SpreadsheetsInit.getService();
        BotdDataExtractor extractor = new BotdDataExtractor(sheets);
        ProgressStorage progressStorage = new LocalFileProgressStorage();
        SpotifyIntegration spotifyIntegration = new SpotifyIntegration();

        BotdData botdData = extractor.extract();
        botdData.getBotdStages().stream()
                .map(BotdStage::getTracks)
                .flatMap(Collection::stream)
                .filter(botdTrack -> !progressStorage.isProcessed(botdTrack))
                .forEach(botdTrack -> {
                    spotifyIntegration.addToPlaylist(botdTrack);
                    progressStorage.markAsProcessed(botdTrack);
                });
    }
}
