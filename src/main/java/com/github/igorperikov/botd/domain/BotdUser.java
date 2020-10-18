package com.github.igorperikov.botd.domain;

public class BotdUser {
    private final String name;
    private final String nickname;

    public BotdUser(String name, String nickname) {
        this.name = name;
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }
}
