package com.github.igorperikov.botd.cache;

import com.github.igorperikov.botd.entity.BotdTrack;
import com.github.igorperikov.botd.entity.SpotifyEntity;

import java.util.List;

/**
 * TODO: save results to optimize api usage and speed
 */
public interface SongCache {
    List<SpotifyEntity> lookup(BotdTrack botdTrack);
}
