package net.lawaxi.lottery.models;

import cn.hutool.json.JSONObject;
import net.lawaxi.lottery.manager.database;

import java.util.HashMap;

public class Wish {
    private static final HashMap<Integer, Wish> wish = new HashMap<>();
    private static final HashMap<Integer, String> lastWishTarget = new HashMap<>();
    private static net.lawaxi.lottery.manager.database database;
    private final int id;
    private final int user_id;
    private final String target;
    private int time = 10;

    private Wish(int id, int user_id, String target) {
        this.id = id;
        this.user_id = user_id;
        this.target = target;
    }

    public Wish(int id, int user_id, String target, int remaining_time) {
        this(id, user_id, target);
        time = remaining_time;
    }

    public Wish(int user_id, String target) throws NullPointerException {
        this(database.applyWish(user_id, target, 10), user_id, target);
    }

    public static HashMap<Integer, Wish> getWish() {
        return wish;
    }

    public static HashMap<Integer, String> getLastWishTarget() {
        return lastWishTarget;
    }

    public static void initWishList(database db) {
        database = db;
        JSONObject[] wishes = db.getAllOngoingWishes();
        for (JSONObject w : wishes) {
            int user_id = w.getInt("user_id");
            wish.put(user_id, new Wish(w.getInt("id"), user_id, w.getStr("wish_target"), w.getInt("remaining_count")));
        }
    }

    public int reduce() {
        database.reduceWishCount(id);
        time--;
        if (time == 0) {
            database.setWishStatus(id, 0);
            wish.remove(user_id);
            lastWishTarget.put(user_id, this.target);
        }
        return time;
    }

    public boolean match(String star_name) {
        if (this.target.equals(star_name)) {
            database.setWishStatus(id, 1);
            wish.remove(user_id);
            lastWishTarget.put(user_id, this.target);
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
