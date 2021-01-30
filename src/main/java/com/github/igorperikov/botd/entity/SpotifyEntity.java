package com.github.igorperikov.botd.entity;

import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Abstraction for either
 * {@link com.wrapper.spotify.model_objects.specification.AlbumSimplified}
 * or
 * {@link com.wrapper.spotify.model_objects.specification.Track}
 */
public class SpotifyEntity extends SpotifyId {
    private final String name;
    private final boolean isTrack;
    private final ArtistSimplified[] artists;

    private SpotifyEntity(String id, String name, boolean isTrack, ArtistSimplified[] artists) {
        super(id);
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

    public String getName() {
        return name;
    }

    public boolean isTrack() {
        return isTrack;
    }

    public ArtistSimplified[] getArtists() {
        return artists;
    }

    @Override
    public String toString() {
        return Arrays.stream(getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(",")) +
                " " +
                getName();
    }
}
