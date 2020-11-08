package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.data.domain.BotdTrack;

import java.util.Optional;

/**
 * TODO: save results to optimize api usage and speed
 */
public interface SongCache {
    Optional<Song> lookup(BotdTrack botdTrack);
}
