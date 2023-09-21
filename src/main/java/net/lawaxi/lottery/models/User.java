package net.lawaxi.lottery.models;

import net.lawaxi.lottery.handler.config;

public class User {
    private static config config;
    public final Long g;
    public final Long m;
    private UserWives wives;

    public User(Long group, Long member) {
        this.g = group;
        this.m = member;
    }

    public int getIndex() {
        return config.getUserIndex(this);
    }

    public void setWives(UserWives wives) {
        this.wives = wives;
    }

    public UserWives getWives() {
        return wives;
    }

    public static void setConfig(config config) {
        User.config = config;
    }

    @Override
    public String toString() {
        return String.format("{\"g\":%d,\"m\":%d}", g, m);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof User){
            return ((User) obj).g==g && ((User) obj).m==m;
        }
        return false;
    }
}
