package net.lawaxi.lottery.models;

import net.lawaxi.lottery.manager.database;

import java.util.HashMap;

public class Chance {
    private static final HashMap<Integer, Integer> chances = new HashMap<>();
    private static net.lawaxi.lottery.manager.database database = null;

    public static HashMap<Integer, Integer> getChances() {
        return chances;
    }

    public static int getUserChance(int user_id) {
        if (database == null) {
            return 0;
        } else {
            return database.getLotteryEntries(user_id);
        }
    }

    public static int add(int user_id, int offset) {
        if (database == null) {
            return -1;
        } else {
            return database.addLotteryEntries(user_id, offset);
        }
    }

    public static int reduce(int user_id) {
        if (database == null) {
            return -1;
        } else {
            return database.spendLotteryEntries(user_id, 1);
        }
    }

    public static void initChance(database db) {
        database = db;
    }
}
