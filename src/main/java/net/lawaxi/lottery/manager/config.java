package net.lawaxi.lottery.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.lottery.handler.PasswordHandler;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class config {
    public static final String API = "https://h5.48.cn/resource/jsonp/allmembers.php?gid=00";
    public final JSONArray starData;
    public final Font font;
    private final File starDataFile;
    private final database database;
    private final PasswordHandler password;
    private String[] sysLottery;
    private String[] sysData;
    private String[] sysMyId;
    private String[] sysRank;
    private String[] sysWish;
    private String[] allowGroup;
    private String[] birthdayBroadcastGroup;

    public config(File config) {
        //config
        Setting s;
        if (!config.exists()) {
            FileUtil.touch(config);

            s = new Setting(config, StandardCharsets.UTF_8, false);
            s.setByGroup("lottery", "system", "来个老婆,换个老婆");
            s.setByGroup("data", "system", "我的老婆");
            s.setByGroup("myId", "system", "我的编号");
            s.setByGroup("rank", "system", "rank");
            s.setByGroup("wish", "system", "许愿");
            s.setByGroup("starDataFile", "system", new File(config.getParent(), "star_data").getAbsolutePath());
            s.setByGroup("birthdayBroadcastFont", "system", "Microsoft YaHei");

            s.setByGroup("log_round", "password", "12");

            s.setByGroup("driver", "database", "sqlite");
            s.setByGroup("file", "database", new File(config.getParent(), "main.db").getAbsolutePath());
            s.setByGroup("host", "database", "");
            s.setByGroup("post", "database", "");
            s.setByGroup("database", "database", "");
            s.setByGroup("account", "database", "");
            s.setByGroup("password", "database", "");

            s.setByGroup("allowGroups", "permission", "");
            s.setByGroup("birthdayBroadcastGroup", "permission", "");

            s.store();
        } else {
            s = new Setting(config, StandardCharsets.UTF_8, false);
        }
        load(s);
        this.starDataFile = new File(s.getStr("starDataFile", "system", new File(config.getParent(), "star_data").getAbsolutePath()));
        this.font = new Font(s.getStr("birthdayBroadcastFont", "system", "Microsoft YaHei"), Font.PLAIN, 50);

        //star_data
        if (!starDataFile.exists()) {
            FileUtil.touch(starDataFile);
            this.starData = new JSONArray();
            downloadStarData();
        } else {
            this.starData = JSONUtil.parseArray(FileUtil.readString(this.starDataFile, "UTF-8"));
        }

        //database
        if (s.getStr("driver", "database", "sqlite").equalsIgnoreCase("mysql")) {
            String host = s.getStr("host", "database", "localhost");
            int post = s.getInt("post", "database", 3306);
            String database = s.getStr("database", "database", "wifelottery");
            String account = s.getStr("account", "database", "");
            String password = s.getStr("password", "database", "");
            this.database = new database(net.lawaxi.lottery.manager.database.initConnection(host, post, database, account, password), 1);
        } else {
            File databaseFile = new File(s.getStr("file", "database", new File(config.getParent(), "main.db").getAbsolutePath()));
            this.database = new database(net.lawaxi.lottery.manager.database.initConnection(databaseFile), 0);
        }

        //password
        this.password = new PasswordHandler(this.database, s.getInt("log_round", "password", 12));
    }

    private void load(Setting s) {
        sysLottery = s.getStrings("lottery", "system");
        sysData = s.getStrings("data", "system");
        sysMyId = s.getStrings("myId", "system");
        sysRank = s.getStrings("rank", "system");
        sysWish = s.getStrings("wish", "system");
        if (sysLottery == null)
            sysLottery = new String[]{"来个老婆", "换个老婆"};
        if (sysData == null)
            sysData = new String[]{"我的老婆"};
        if (sysMyId == null)
            sysMyId = new String[]{"我的编号"};
        if (sysRank == null)
            sysRank = new String[]{"rank"};
        if (sysWish == null)
            sysWish = new String[]{"许愿"};

        this.allowGroup = s.getStrings("allowGroups", "permission");
        if (allowGroup == null)
            allowGroup = new String[]{};
        this.birthdayBroadcastGroup = s.getStrings("birthdayBroadcastGroups", "permission");
        if (birthdayBroadcastGroup == null)
            birthdayBroadcastGroup = new String[]{};
    }

    public String downloadStarData() {
        JSONArray originalStarData = new JSONArray(this.starData);
        String lastTime = "-ori";
        try {
            FileTime t = Files.readAttributes(this.starDataFile.toPath(), BasicFileAttributes.class)
                    .lastModifiedTime();
            lastTime = DateUtil.format(new Date(t.toMillis()), "-yyyyMMdd");
        } catch (IOException e) {

        }
        this.starData.clear();

        this.starData.add(JSONUtil.parse("{\"s\":\"鞠婧祎\",\"sid\":\"10027\",\"n\":\"小鞠\",\"g\":\"明星殿堂\",\"t\":\"\",\"p\":\"SNH48 二期生\",\"i\":\"0\",\"birthday\":\"06.18\"}"));
        this.starData.add(JSONUtil.parse("{\"s\":\"李艺彤\",\"sid\":\"10031\",\"n\":\"发卡\",\"g\":\"明星殿堂\",\"t\":\"\",\"p\":\"SNH48 二期生\",\"i\":\"0\",\"birthday\":\"12.23\"}"));

        String j = HttpRequest.get(API)
                .header("Host", "h5.48.cn").header("Connection", "keep-alive").header("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"").header("sec-ch-ua-mobile", "?0").header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36").header("sec-ch-ua-platform", "\"Windows\"").header("Accept", "*/*").header("Sec-Fetch-Site", "cross-site").header("Sec-Fetch-Mode", "no-cors").header("Sec-Fetch-Dest", "script").header("Referer", "https://www.snh48.com/").header("Accept-Encoding", "gzip, deflate, br").header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-TW;q=0.6")
                .execute().body();

        for (Object j1 : JSONUtil.parseObj(j).getJSONArray("rows").toArray(new Object[0])) {
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

        AtomicBoolean changed = new AtomicBoolean(false);

        java.util.List<String> addedMembers = this.starData.stream()
                .filter(updatedMember -> originalStarData.stream()
                        .noneMatch(originalMember -> ((JSONObject) originalMember).getStr("sid").equals(((JSONObject) updatedMember).getStr("sid"))))
                .map(updatedMember -> {
                    changed.set(true);
                    return "\n+ " + ((JSONObject) updatedMember).getStr("s");
                })
                .collect(Collectors.toList());

        java.util.List<String> removedMembers = originalStarData.stream()
                .filter(originalMember -> this.starData.stream()
                        .noneMatch(updatedMember -> ((JSONObject) updatedMember).getStr("sid").equals(((JSONObject) originalMember).getStr("sid"))))
                .map(originalMember -> {
                    changed.set(true);
                    return "\n- " + ((JSONObject) originalMember).getStr("s");
                })
                .collect(Collectors.toList());

        if (changed.get()) {
            String backupFile = starDataFile.getPath() + lastTime;
            FileUtil.writeString(originalStarData.toString(), backupFile, "UTF-8");
            return "已更新成员列表，当前数据总数 " + this.starData.size() + " 人"
                    + String.join("", addedMembers)
                    + String.join("", removedMembers)
                    + "\n旧数据已备份至" + backupFile;
        } else {
            return "成员列表已是最新，当前数据总数 " + this.starData.size() + " 人";
        }
    }

    public File getStarDataFile() {
        return starDataFile;
    }

    public net.lawaxi.lottery.manager.database getDatabase() {
        return database;
    }

    public PasswordHandler getPassword() {
        return password;
    }

    public String[] getSysLottery() {
        return sysLottery;
    }

    public String[] getSysData() {
        return sysData;
    }

    public String[] getSysMyId() {
        return sysMyId;
    }

    public String[] getSysRank() {
        return sysRank;
    }

    public String[] getSysWish() {
        return sysWish;
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

    public JSONArray getStarData() {
        return starData;
    }
}