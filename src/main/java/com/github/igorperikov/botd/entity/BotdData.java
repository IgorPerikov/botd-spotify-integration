package com.github.igorperikov.botd.entity;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BotdData {
    private final List<BotdStage> botdStages;
    private final BotdUsers users;

    public BotdData(List<BotdStage> botdStages, BotdUsers users) {
        this.botdStages = botdStages;
        this.users = users;
    }

    public List<BotdStage> getBotdStages() {
        return botdStages;
    }

    public BotdUsers getUsers() {
        return users;
    }

    public List<BotdTrack> getAllTracks() {
        return getBotdStages().stream()
                .map(BotdStage::getTracks)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public String getMd5() {
        return DigestUtils.md5Hex(getAllTracks().stream().map(Object::toString).collect(Collectors.joining(";")));
    }
}
