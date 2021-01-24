package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.accuracy.AccuracyService;
import com.github.igorperikov.botd.accuracy.DistanceCalculationUtils;
import com.github.igorperikov.botd.cache.SongCache;
import com.github.igorperikov.botd.entity.BotdTrack;
import com.github.igorperikov.botd.entity.PlaylistItems;
import com.github.igorperikov.botd.entity.SpotifyEntity;
import com.github.igorperikov.botd.entity.SpotifyId;
import com.github.igorperikov.botd.telegram.TelegramMessageSender;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authorization url
 * https://accounts.spotify.com/authorize?client_id=6624e196221e489a87490750cfa34354&response_type=code&redirect_uri=https%3A%2F%2Fthis-is-my-website.io&scope=playlist-modify-public
 */
public class SpotifyApiService {
    private static final Logger log = LoggerFactory.getLogger(SpotifyApiService.class);

    private static final String PLAYLIST_ID = "55RwDsEAaMLq4iVFnRrxFc";
    private static final int NUMBER_OF_ITEMS_TO_SEARCH_FOR = 20;

    private final SpotifyApi spotifyApi;
    private final SongCache songCache;
    private final AccuracyService accuracyService;
    private final TelegramMessageSender telegramMessageSender;

    public SpotifyApiService(
            SpotifyApi spotifyApi,
            SongCache songCache,
            AccuracyService accuracyService,
            TelegramMessageSender telegramMessageSender
    ) {
        this.spotifyApi = spotifyApi;
        this.songCache = songCache;
        this.accuracyService = accuracyService;
        this.telegramMessageSender = telegramMessageSender;
    }

    /**
     * @return true if something was found and added
     */
    public boolean add(BotdTrack botdTrack) {
        List<? extends SpotifyId> songsToAdd = songCache.lookup(botdTrack);
        if (songsToAdd == null) {
            songsToAdd = find(botdTrack);
            if (songsToAdd.isEmpty()) {
                telegramMessageSender.sendToContentManager(String.format("%s not found", botdTrack));
            }
            songCache.save(botdTrack, songsToAdd);
        } else {
            log.info("{} found in song cache", botdTrack);
        }
        addToPlaylist(songsToAdd);
        return !songsToAdd.isEmpty();
    }

    public void deleteAllSongsFromPlaylist() {
        int limit = 100; // max allowed by spotify api
        List<PlaylistTrack> playlistItems = getAllPlaylistItems(limit);
        deletePlaylistItems(playlistItems, limit);
    }

    private List<PlaylistTrack> getAllPlaylistItems(int limit) {
        int offset = 0;
        List<PlaylistTrack> tracks = new ArrayList<>();
        boolean hasMore = true;
        while (hasMore) {
            PlaylistItems playlistItems = getPlaylistItems(limit, offset);
            Collections.addAll(tracks, playlistItems.getTracks());
            hasMore = playlistItems.isHasMore();
            offset += limit;
        }
        return tracks;
    }

    private PlaylistItems getPlaylistItems(int limit, int offset) {
        try {
            Paging<PlaylistTrack> paging = spotifyApi.getPlaylistsItems(PLAYLIST_ID)
                    .limit(limit)
                    .offset(offset)
                    .build()
                    .execute();
            return new PlaylistItems(paging.getItems(), paging.getNext() != null);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void deletePlaylistItems(List<PlaylistTrack> allItems, int limit) {
        for (List<PlaylistTrack> partitionItems : Lists.partition(allItems, limit)) {
            try {
                JsonArray uris = new JsonArray();
                for (PlaylistTrack partitionItem : partitionItems) {
                    String uri = ((Track) partitionItem.getTrack()).getUri();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("uri", new JsonPrimitive(uri));
                    uris.add(jsonObject);
                }
                spotifyApi.removeItemsFromPlaylist(PLAYLIST_ID, uris).build().execute();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<SpotifyEntity> find(BotdTrack botdTrack) {
        List<SpotifyEntity> candidates = findCandidates(botdTrack);
        if (candidates.size() == 0) {
            log.error("{} not found on spotify", botdTrack);
            return Collections.emptyList();
        }
        return accuracyService.findBest(botdTrack, candidates)
                .map(
                        spotifyEntity -> {
                            if (spotifyEntity.isTrack()) {
                                return Collections.singletonList(spotifyEntity);
                            } else {
                                return getTracksOfAlbum(spotifyEntity);
                            }
                        }
                ).orElse(Collections.emptyList());
    }

    private List<SpotifyEntity> findCandidates(BotdTrack botdTrack) {
        if (botdTrack.isAlbum()) {
            return Arrays.stream(findAlbumCandidates(botdTrack.getFullName()))
                    .map(SpotifyEntity::fromAlbum)
                    .collect(Collectors.toList());
        } else {
            return Arrays.stream(findSongCandidates(botdTrack.getFullName()))
                    .map(SpotifyEntity::fromTrack)
                    .collect(Collectors.toList());
        }
    }

    private Track[] findSongCandidates(String query) {
        Track[] foundTracks = findSongs(query);
        if (foundTracks.length == 0) {
            foundTracks = findSongs(DistanceCalculationUtils.removeParenthesesContent(query));
            if (foundTracks.length == 0) {
                return new Track[0];
            }
        }
        return foundTracks;
    }

    private Track[] findSongs(String query) {
        try {
            return spotifyApi.searchTracks(query)
                    .market(CountryCode.RU)
                    .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                    .build()
                    .execute()
                    .getItems();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private AlbumSimplified[] findAlbumCandidates(String query) {
        try {
            return spotifyApi.searchAlbums(query)
                    .market(CountryCode.RU)
                    .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                    .build()
                    .execute()
                    .getItems();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private List<SpotifyEntity> getTracksOfAlbum(SpotifyId spotifyId) {
        try {
            return Arrays.stream(
                    spotifyApi.getAlbumsTracks(spotifyId.getId())
                            .market(CountryCode.RU)
                            .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                            .build()
                            .execute()
                            .getItems())
                    .map(SpotifyEntity::fromTrack)
                    .collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void addToPlaylist(List<? extends SpotifyId> songs) {
        if (songs.isEmpty()) return;
        try {
            spotifyApi.addItemsToPlaylist(
                    PLAYLIST_ID,
                    songs.stream().map(SpotifyId::getId).toArray(String[]::new)
            ).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
