package com.github.igorperikov.botd.data.domain;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BotdUser)) return false;
        BotdUser botdUser = (BotdUser) o;
        return Objects.equals(name, botdUser.name) &&
                Objects.equals(nickname, botdUser.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nickname);
    }
}
