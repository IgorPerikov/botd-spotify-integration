package com.github.igorperikov.botd.restart;

import com.github.igorperikov.botd.spotify.SpotifyApiService;
import com.github.igorperikov.botd.storage.ProgressStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupService {
    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);

    private final SpotifyApiService spotifyApiService;
    private final ProgressStorage progressStorage;

    public CleanupService(SpotifyApiService spotifyApiService, ProgressStorage progressStorage) {
        this.spotifyApiService = spotifyApiService;
        this.progressStorage = progressStorage;
    }

    public void cleanup() {
        log.info("Start removing tracks from playlist");
        spotifyApiService.deleteAllSongsFromPlaylist();
        log.info("Finish removing tracks from playlist");
        log.info("Start deleting progress from progress storage");
        progressStorage.deleteAllProgress();
        log.info("Finish deleting progress from progress storage");
    }
}
