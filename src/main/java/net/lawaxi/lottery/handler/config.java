package net.lawaxi.lottery.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.lottery.models.User;
import net.lawaxi.lottery.models.UserWives;
import net.lawaxi.lottery.models.Wife;

import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class config {
    public static final String API = "https://h5.48.cn/resource/jsonp/allmembers.php?gid=00";
    public final JSONArray starData;
    public final Font font;
    private final File starDataFile;
    private final Setting s;
    //users
    private final List<User> users = new ArrayList<>();
    private final List<UserWives> wives = new ArrayList<>();
    //wives
    private final HashMap<Integer, Wife> wiveModels = new HashMap<>();
    private String[] sysLottery;
    private String[] sysData;
    private String[] allowGroup;
    private String[] birthdayBroadcastGroup;

    public config(File config, File starData) {
        this.starDataFile = starData;
        this.s = new Setting(config, StandardCharsets.UTF_8, false);

        if (!config.exists()) {
            FileUtil.touch(config);

            s.setByGroup("lottery", "system", "来个老婆,换个老婆");
            s.setByGroup("data", "system", "我的老婆");
            s.setByGroup("birthdayBroadcastFont", "system", "Microsoft YaHei");
            s.setByGroup("users", "users", "[]");
            s.setByGroup("wives", "users", "[]");
            s.setByGroup("sense", "wives", "{}");
            s.setByGroup("allowGroups", "permission", "");
            s.setByGroup("birthdayBroadcastGroup", "permission", "");

            s.store();
        }
        if (!starData.exists()) {
            FileUtil.touch(starData);
            this.starData = new JSONArray();
            downloadStarData();
        } else {
            this.starData = JSONUtil.parseArray(FileUtil.readString(this.starDataFile, "UTF-8"));
        }

        font = new Font(s.getStr("birthdayBroadcastFont", "system", "Microsoft YaHei"), Font.PLAIN, 100);
        load();
    }

    private void load() {
        sysLottery = s.getStrings("lottery", "system");
        sysData = s.getStrings("data", "system");
        if (sysLottery == null)
            sysLottery = new String[]{"来个老婆", "换个老婆"};
        if (sysData == null)
            sysData = new String[]{"我的老婆"};

        this.allowGroup = s.getStrings("allowGroups", "permission");
        if (allowGroup == null)
            allowGroup = new String[]{};
        this.birthdayBroadcastGroup = s.getStrings("birthdayBroadcastGroups", "permission");
        if (birthdayBroadcastGroup == null)
            birthdayBroadcastGroup = new String[]{};

        //users
        for (Object o : JSONUtil.parseArray(s.getByGroup("users", "users")).toArray()) {
            JSONObject o1 = JSONUtil.parseObj(o);
            users.add(new User(o1.getLong("g"), o1.getLong("m")));
        }

        for (Object o : JSONUtil.parseArray(s.getByGroup("wives", "users")).toArray()) {
            JSONObject o1 = JSONUtil.parseObj(o);
            if (o1.keySet() == null)
                continue;

            UserWives w = new UserWives();
            wives.add(w);
            for (String key : o1.keySet()) {
                w.put(key, o1.getInt(key));
            }
        }

        //wives
        JSONObject o = JSONUtil.parseObj(s.getByGroup("sense", "wives"));
        for (String sid : o.keySet()) {
            JSONObject wive = o.getJSONObject(sid);
            HashMap<Long, Long[]> a = new HashMap();
            for (String group : wive.keySet()) {
                a.put(Long.valueOf(group), wive.getBeanList(group, Long.class).toArray(new Long[0]));
            }
            wiveModels.put(Integer.valueOf(sid), new Wife(a));
        }

    }

    public void save() {
        s.setByGroup("users", "users", listToJson(users));
        s.setByGroup("wives", "users", listToJson(wives));
        s.setByGroup("sense", "wives", senseToJson());

        s.store();
    }

    public int downloadStarData() {
        this.starData.clear();

        this.starData.add(JSONUtil.parse("{\"s\":\"鞠婧祎\",\"sid\":\"10027\",\"n\":\"小鞠\",\"g\":\"明星殿堂\",\"t\":\"\",\"p\":\"SNH48 二期生\",\"i\":\"0\",\"birthday\":\"06.18\"}"));
        this.starData.add(JSONUtil.parse("{\"s\":\"李艺彤\",\"sid\":\"10031\",\"n\":\"发卡\",\"g\":\"明星殿堂\",\"t\":\"\",\"p\":\"SNH48 二期生\",\"i\":\"0\",\"birthday\":\"12.23\"}"));

        String j = HttpRequest.get(API)
                .header("Host", "h5.48.cn").header("Connection", "keep-alive").header("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"").header("sec-ch-ua-mobile", "?0").header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36").header("sec-ch-ua-platform", "\"Windows\"").header("Accept", "*/*").header("Sec-Fetch-Site", "cross-site").header("Sec-Fetch-Mode", "no-cors").header("Sec-Fetch-Dest", "script").header("Referer", "https://www.snh48.com/").header("Accept-Encoding", "gzip, deflate, br").header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-TW;q=0.6")
                .execute().body();

        int count = 0;
        for (Object j1 : JSONUtil.parseObj(j).getJSONArray("rows").toArray(new Object[0])) {
            count++;
            JSONObject o1 = JSONUtil.parseObj(j1);
            JSONObject o = new JSONObject();
            o.set("s", o1.getStr("sname"));
            o.set("sid", o1.getStr("sid"));
            o.set("n", o1.getStr("nickname"));
            o.set("g", o1.getStr("gname")); //团
            o.set("t", o1.getStr("tname")); //队伍
            o.set("p", o1.getStr("pname")); //期数
            o.set("i", o1.getStr("pocket_id"));
            o.set("birthday", o1.getStr("birth_day"));
            this.starData.add(o);
        }

        FileUtil.writeString(starData.toString(), starDataFile, "UTF-8");
        return count;
    }

    public String[] getSysLottery() {
        return sysLottery;
    }

    public String[] getSysData() {
        return sysData;
    }

    public boolean doesGroupAllowed(long id) {
        for (String g : allowGroup) {
            if (g.equals(String.valueOf(id)))
                return true;
        }
        return false;
    }

    public String[] getBirthdayBroadcastGroup() {
        return birthdayBroadcastGroup;
    }

    public boolean doesGroupAllowedBroadcast(long id) {
        for (String g : birthdayBroadcastGroup) {
            if (g.equals(String.valueOf(id)))
                return true;
        }
        return false;
    }

    public boolean allowed() {
        return allowGroup.length > 0;
    }

    public boolean allowedBroadcast() {
        return birthdayBroadcastGroup.length > 0;
    }

    private String listToJson(List l) {
        String out = "[";
        for (Object o : l) {
            out += o.toString() + ",";
        }
        if (out.length() > 1)
            return out.substring(0, out.length() - 1) + "]";
        return "[]";
    }

    private String senseToJson() {
        String out = "{";
        for (int sid : this.wiveModels.keySet()) {
            out += "\"" + sid + "\":" + this.wiveModels.get(sid).toString() + ",";
        }
        if (out.length() > 1)
            return out.substring(0, out.length() - 1) + "}";
        return "{}";
    }

    public int getUserIndex(User user) {
        return this.users.indexOf(user);
    }

    public User getUserByIndex(int index) {
        if (index >= 0 && index < this.users.size()) {
            return users.get(index);
        }
        return null;
    }

    public void addNewWive(User user, String wife) {
        if (user.getIndex()== -1) {
            users.add(user);
            wives.add(new UserWives());
        }

        wives.get(user.getIndex()).add(wife);
        save();
    }

    public UserWives getUserWives(User user) {
        if (user.getIndex()== -1) {
            users.add(user);
            wives.add(new UserWives());
            save();
        }

        return wives.get(user.getIndex());
    }

    public JSONArray getStarData() {
        return starData;
    }

    //若为成员历史情愫新高则返回true
    public int getHistorySense(int sid, long group) {
        Wife w = this.wiveModels.get(sid);
        if (w != null) {
            return w.getSenseInGroup(group);
        }
        return 0;
    }

    public boolean testWifeModel(int sid, User user, int sense) {
        Wife w = this.wiveModels.get(sid);
        if (w == null) {
            this.wiveModels.put(sid, new Wife(new HashMap<>()));
        } else if (sense <= w.getSenseInGroup(user.g)) {
            return false;
        }

        this.wiveModels.get(sid).putSense(user.g, user.m, sense);
        return true;
    }

    public Wife getWifeModel(int sid) {
        return this.wiveModels.get(sid);
    }
}
