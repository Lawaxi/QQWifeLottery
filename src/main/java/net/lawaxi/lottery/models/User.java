package net.lawaxi.lottery.models;

import net.lawaxi.lottery.handler.config;

public class User {
    private static config config;
    public final Long g;
    public final Long m;
    public final int index;

    public User(Long group, Long member) {
        this.g = group;
        this.m = member;
        this.index = config.getUserIndex(g, m);
    }

    public User(User user) {
        this(user.g, user.m);
    }

    public static void setConfig(config config) {
        User.config = config;
    }

    @Override
    public String toString() {
        return String.format("{\"g\":%d,\"m\":%d}", g, m);
    }
}
