package com.github.igorperikov.botd.entity;

import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;

/**
 * Abstraction for either
 * {@link com.wrapper.spotify.model_objects.specification.AlbumSimplified}
 * or
 * {@link com.wrapper.spotify.model_objects.specification.Track}
 */
public class SpotifyEntity {
    /**
     * uri for song, id for album
     */
    private final String id;
    private final String name;
    private final boolean isTrack;
    private final ArtistSimplified[] artists;

    private SpotifyEntity(String id, String name, boolean isTrack, ArtistSimplified[] artists) {
        this.id = id;
        this.name = name;
        this.isTrack = isTrack;
        this.artists = artists;
    }

    public static SpotifyEntity fromTrack(Track track) {
        return new SpotifyEntity(track.getUri(), track.getName(), true, track.getArtists());
    }

    public static SpotifyEntity fromTrack(TrackSimplified track) {
        return new SpotifyEntity(track.getUri(), track.getName(), false, track.getArtists());
    }

    public static SpotifyEntity fromAlbum(AlbumSimplified album) {
        return new SpotifyEntity(album.getId(), album.getName(), false, album.getArtists());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isTrack() {
        return isTrack;
    }

    public ArtistSimplified[] getArtists() {
        return artists;
    }
}
