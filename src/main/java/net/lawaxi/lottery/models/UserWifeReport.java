package net.lawaxi.lottery.models;

import cn.hutool.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class UserWifeReport {
    private final int total;
    private final int total_bring;
    private final List<WifeReport> wives;
    private final List<WifeReport> wives_sort_by_sense;

    public UserWifeReport(JSONObject[] userWives) {
        total = userWives.length;

        HashMap<Integer, WifeReport> report = new HashMap<>();
        for (JSONObject userWife : userWives) {
            int sense = userWife.getInt("sense");
            if (sense < 80)
                continue;

            int sid = userWife.getInt("wife_id");
            if (report.containsKey(sid)) {
                report.get(sid).addRecord(sense);
            } else {
                report.put(sid, new WifeReport(userWife.getStr("wife_name"), sense));
            }
        }
        wives = new ArrayList<>(report.values());
        Collections.sort(wives, (w1, w2) -> Integer.compare(w2.count, w1.count));
        wives_sort_by_sense = new ArrayList<>(wives);
        Collections.sort(wives_sort_by_sense, (w1, w2) -> Integer.compare(w2.sense, w1.sense));
        total_bring = wives.size();
    }

    public List<WifeReport> getWives() {
        return wives;
    }

    public List<WifeReport> getWivesSortBySense() {
        return wives_sort_by_sense;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalBring() {
        return total_bring;
    }
}
