package com.github.igorperikov.botd.cache;

import com.github.igorperikov.botd.entity.BotdTrack;
import com.github.igorperikov.botd.entity.SpotifyId;
import com.github.igorperikov.botd.storage.ResourceFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceFileSongCache extends ResourceFileStorage implements SongCache {
    private static final Logger log = LoggerFactory.getLogger(ResourceFileSongCache.class);

    private static final String KEY_VALUE_SEPARATOR = " -> ";
    private static final String MULTIVALUE_SEPARATOR = ",";

    private final Map<String, List<? extends SpotifyId>> cache = new HashMap<>();

    public ResourceFileSongCache() {
        super("song_cache.txt");
        createIfNotExists();
        populateCacheMap();
    }

    @Override
    public List<? extends SpotifyId> lookup(BotdTrack botdTrack) {
        return cache.get(getId(botdTrack));
    }

    @Override
    public void save(BotdTrack botdTrack, List<? extends SpotifyId> spotifyIds) {
        append(createRecord(botdTrack, spotifyIds));
        cache.put(botdTrack.getFullName(), spotifyIds);
    }

    private void populateCacheMap() {
        List<String> strings = readAll();
        for (String record : strings) {
            if (!record.contains(KEY_VALUE_SEPARATOR)) {
                log.error("Incorrect data: '{}' should contain {}", record, KEY_VALUE_SEPARATOR);
            }
            String[] kvSplit = record.split(KEY_VALUE_SEPARATOR);
            if (kvSplit.length == 2) {
                cache.put(
                        kvSplit[0],
                        Arrays.stream(kvSplit[1].split(MULTIVALUE_SEPARATOR)).map(SpotifyId::new).collect(Collectors.toList())
                );
            }
            if (kvSplit.length == 1) {
                cache.put(kvSplit[0], Collections.emptyList());
            }
        }
    }

    private String getId(BotdTrack botdTrack) {
        return botdTrack.getFullName() + " " + botdTrack.isAlbum();
    }

    private String createRecord(BotdTrack botdTrack, List<? extends SpotifyId> spotifyIds) {
        return getId(botdTrack) +
                KEY_VALUE_SEPARATOR +
                spotifyIds.stream().map(SpotifyId::getId).collect(Collectors.joining(MULTIVALUE_SEPARATOR)) +
                System.lineSeparator();
    }
}
