package net.lawaxi.lottery.models;

import java.util.ArrayList;
import java.util.List;

public class UserWifeReport {
    public final UserWives userWives;
    private final int total;
    private final List<Long> wives; //降序

    public UserWifeReport(UserWives userWives) {
        this.userWives = userWives;

        wives = new ArrayList<>(userWives.keySet());
        wives.sort((o1, o2) -> userWives.get(o2) - userWives.get(o1));


        int count = 0;
        for (long key : userWives.keySet()) {
            count += userWives.get(key);
        }
        total = count;
    }

    public List<Long> getWives() {
        return wives;
    }

    public int getTotal() {
        return total;
    }

    public int getWifeTotal() {
        return wives.size();
    }

    public int getCount(Long wife) {
        return userWives.get(wife);
    }
}
