package net.lawaxi.lottery.models;

import cn.hutool.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserMaxSenseReport {
    private final List<WifeReport> wives;

    public UserMaxSenseReport(JSONObject[] userWives) {
        this.wives = Arrays.stream(userWives)
                .map(json -> new WifeReport(json.getStr("wife_name"), json.getInt("sense")))
                .collect(Collectors.toList());
        Collections.sort(wives, (w1, w2) -> Integer.compare(w2.sense, w1.sense));
    }

    public List<WifeReport> getWives() {
        return wives;
    }

    public int getTotal() {
        return wives.size();
    }
}
