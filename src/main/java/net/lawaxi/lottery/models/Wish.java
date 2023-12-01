package net.lawaxi.lottery.models;

import java.util.HashMap;

public class Wish {
    private static HashMap<Integer, Wish> wish = null;
    private static HashMap<Integer, String> lastWishTarget = new HashMap<>();
    private final int id;
    private final String target;
    private int time = 10;

    public Wish(int id, String target) throws Exception {
        this.id = id;
        this.target = target;
        if (wish == null) {
            throw new Exception("未设置许愿集合");
        }
    }

    public static void setWish(HashMap<Integer, Wish> wish) {
        Wish.wish = wish;
    }

    public static void setLastWishTarget(HashMap<Integer, String> lastWishTarget) {
        Wish.lastWishTarget = lastWishTarget;
    }

    public int reduce() {
        time--;
        if (time == 0) {
            wish.remove(id);
            lastWishTarget.put(id, this.target);
        }
        return time;
    }

    public boolean match(String star_name) {
        if (this.target.equals(star_name)) {
            wish.remove(id);
            lastWishTarget.put(id, this.target);
            return true;
        }
        return false;
    }

    public int getTimeLast() {
        return time;
    }

    public String getTarget() {
        return target;
    }
}
