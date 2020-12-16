package com.github.igorperikov.botd.entity;

import com.wrapper.spotify.model_objects.specification.PlaylistTrack;

public class PlaylistItems {
    private final PlaylistTrack[] tracks;
    private final boolean hasMore;

    public PlaylistItems(PlaylistTrack[] tracks, boolean hasMore) {
        this.tracks = tracks;
        this.hasMore = hasMore;
    }

    public PlaylistTrack[] getTracks() {
        return tracks;
    }

    public boolean isHasMore() {
        return hasMore;
    }
}
