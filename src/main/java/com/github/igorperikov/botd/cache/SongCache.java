package com.github.igorperikov.botd.cache;

import com.github.igorperikov.botd.entity.BotdTrack;
import com.github.igorperikov.botd.entity.SpotifyId;

import java.util.List;

public interface SongCache {
    List<? extends SpotifyId> lookup(BotdTrack botdTrack);

    void save(BotdTrack botdTrack, List<? extends SpotifyId> spotifyIds);
}
