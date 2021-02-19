package com.github.igorperikov.botd.restart;

import com.github.igorperikov.botd.execution.AppExecutionContext;
import com.github.igorperikov.botd.spotify.SpotifyApiService;
import com.github.igorperikov.botd.storage.ProgressStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupService {
    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);

    private final SpotifyApiService spotifyApiService;
    private final ProgressStorage progressStorage;
    private final AppExecutionContext context;

    public CleanupService(
            SpotifyApiService spotifyApiService,
            ProgressStorage progressStorage,
            AppExecutionContext context
    ) {
        this.spotifyApiService = spotifyApiService;
        this.progressStorage = progressStorage;
        this.context = context;
    }

    public void cleanup() {
        log.info("Start removing tracks from playlist");
        int tracksBefore = spotifyApiService.deleteAllSongsFromPlaylist();
        context.registerRestart(tracksBefore);
        log.info("Finish removing tracks from playlist");
        log.info("Start deleting progress from progress storage");
        progressStorage.deleteAllProgress();
        log.info("Finish deleting progress from progress storage");
    }
}
