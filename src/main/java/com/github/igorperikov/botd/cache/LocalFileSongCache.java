package com.github.igorperikov.botd.cache;

import com.github.igorperikov.botd.entity.BotdTrack;
import com.github.igorperikov.botd.entity.SpotifyId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class LocalFileSongCache implements SongCache {
    private static final String KEY_VALUE_SEPARATOR = " -> ";
    private static final String MULTIVALUE_SEPARATOR = ",";
    private static final Path LOCAL_FILE_PATH = Path.of(
            "/",
            "Users",
            "igorperikov",
            "idea_projects",
            "botd_spotify_integration",
            "src",
            "main",
            "resources",
            "song_cache.txt"
    );

    private final Map<String, List<? extends SpotifyId>> cache = new HashMap<>();

    public LocalFileSongCache() {
        try {
            if (Files.notExists(LOCAL_FILE_PATH)) {
                Files.createFile(LOCAL_FILE_PATH);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        populateCacheMap();
    }

    @Override
    public List<? extends SpotifyId> lookup(BotdTrack botdTrack) {
        return cache.get(getId(botdTrack));
    }

    @Override
    public void save(BotdTrack botdTrack, List<? extends SpotifyId> spotifyIds) {
        try {
            Files.writeString(
                    LOCAL_FILE_PATH,
                    createRecord(botdTrack, spotifyIds),
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cache.put(botdTrack.getFullName(), spotifyIds);
    }

    private void populateCacheMap() {
        try {
            List<String> strings = Files.readAllLines(LOCAL_FILE_PATH);
            for (String record : strings) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
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
