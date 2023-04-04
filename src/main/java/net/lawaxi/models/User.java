package net.lawaxi.models;

public class User {
    public final Long g;
    public final Long m;

    public User(Long group, Long member) {
        this.g = group;
        this.m = member;
    }

    @Override
    public String toString() {
        return String.format("{\"g\":%d,\"m\":%d}",g,m);
    }
}
