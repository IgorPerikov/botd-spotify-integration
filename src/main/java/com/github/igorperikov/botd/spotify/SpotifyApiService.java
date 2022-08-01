package com.github.igorperikov.botd.spotify;

import com.github.igorperikov.botd.RetryUtils;
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
import com.wrapper.spotify.requests.data.AbstractDataRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
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

    private static final CountryCode MARKET = CountryCode.SE;

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
     * @return number of tracks added
     */
    public int add(BotdTrack botdTrack) {
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
        return songsToAdd.size();
    }

    /**
     * @return number of tracks that existed in playlist before deletion
     */
    public int deleteAllSongsFromPlaylist() {
        int limit = 100; // max allowed by spotify api
        List<PlaylistTrack> playlistItems = getAllPlaylistItems(limit);
        deletePlaylistItems(playlistItems, limit);
        return playlistItems.size();
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
        GetPlaylistsItemsRequest request = spotifyApi.getPlaylistsItems(PLAYLIST_ID)
                .limit(limit)
                .offset(offset)
                .build();
        Paging<PlaylistTrack> paging = executeWithRetry(request);
        return new PlaylistItems(paging.getItems(), paging.getNext() != null);
    }

    private void deletePlaylistItems(List<PlaylistTrack> allItems, int limit) {
        for (List<PlaylistTrack> partitionItems : Lists.partition(allItems, limit)) {
            JsonArray uris = new JsonArray();
            for (PlaylistTrack partitionItem : partitionItems) {
                String uri = ((Track) partitionItem.getTrack()).getUri();
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("uri", new JsonPrimitive(uri));
                uris.add(jsonObject);
            }
            RemoveItemsFromPlaylistRequest request = spotifyApi.removeItemsFromPlaylist(PLAYLIST_ID, uris).build();
            executeWithRetry(request);
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
        SearchTracksRequest request = spotifyApi.searchTracks(query)
                .market(MARKET)
                .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                .build();
        return executeWithRetry(request).getItems();
    }

    private AlbumSimplified[] findAlbumCandidates(String query) {
        SearchAlbumsRequest request = spotifyApi.searchAlbums(query)
                .market(MARKET)
                .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                .build();
        return executeWithRetry(request).getItems();
    }

    private List<SpotifyEntity> getTracksOfAlbum(SpotifyId spotifyId) {
        GetAlbumsTracksRequest request = spotifyApi.getAlbumsTracks(spotifyId.getId())
                .market(MARKET)
                .limit(NUMBER_OF_ITEMS_TO_SEARCH_FOR)
                .build();
        return Arrays.stream(executeWithRetry(request).getItems())
                .map(SpotifyEntity::fromTrack)
                .collect(Collectors.toList());
    }

    private void addToPlaylist(List<? extends SpotifyId> songs) {
        if (songs.isEmpty()) return;
        AddItemsToPlaylistRequest request = spotifyApi.addItemsToPlaylist(
                PLAYLIST_ID,
                songs.stream().map(SpotifyId::getId).toArray(String[]::new)
        ).build();
        executeWithRetry(request);
    }

    private static <T> T executeWithRetry(AbstractDataRequest<T> request) {
        return RetryUtils.execute(
                () -> {
                    try {
                        return request.execute();
                    } catch (IOException | SpotifyWebApiException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                },
                2
        );
    }
}
