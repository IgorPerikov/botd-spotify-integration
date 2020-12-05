package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.data.domain.BotdTrack;

import java.util.List;

/**
 * TODO: save results to optimize api usage and speed
 */
public interface SongCache {
    List<SpotifyEntity> lookup(BotdTrack botdTrack);
}
