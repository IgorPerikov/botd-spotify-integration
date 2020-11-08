package com.github.igorperikov.botd.data.domain;

import java.util.List;

public class BotdUsers {
    private final List<BotdUser> rawUsers;

    public BotdUsers(List<BotdUser> rawUsers) {
        this.rawUsers = rawUsers;
    }

    public List<BotdUser> getAll() {
        return rawUsers;
    }

//    public BotdUser findByName(String name) {
//
//    }
//
//    public BotdUser findByNickname(String nickname) {
//
//    }
}
