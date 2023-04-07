package net.lawaxi.lottery;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.lottery.models.User;
import net.lawaxi.lottery.models.UserWives;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class config {

    private final File config;
    private final File starDataFile;
    public final ArrayList<String> starData;
    private final Setting s;
    private String[] sysLottery = {};
    private String[] sysData = {};
    private List<User> users = new ArrayList<>();
    private List<UserWives> wives = new ArrayList<>();

    public config(File config, File starData) {
        this.config = config;
        this.starDataFile = starData;
        this.s = new Setting(config, StandardCharsets.UTF_8, false);

        if (!config.exists()) {
            FileUtil.touch(config);

            s.setByGroup("lottery","system","来个老婆,换个老婆");
            s.setByGroup("data","system","我的老婆");
            s.setByGroup("users", "users", "[]");
            s.setByGroup("wives", "wives", "[]");

            s.store();
        }
        if(!starData.exists()){
            FileUtil.touch(starData);
            this.starData = new ArrayList<>();
            downloadStarData();
        }else{
            this.starData = new ArrayList<>(Arrays.asList(JSONUtil.parseArray(FileUtil.readString(this.starDataFile, "UTF-8")).toArray(new String[0])));
        }

        load();
    }

    private void load(){

        sysLottery = s.getStrings("lottery","system");
        sysData = s.getStrings("data","system");
        if(sysLottery == null)
            sysLottery = new String[]{"来个老婆","换个老婆"};
        if(sysData == null)
            sysData = new String[]{"我的老婆"};

        for (Object o : JSONUtil.parseArray(s.getByGroup("users", "users")).toArray()) {
            JSONObject o1 = JSONUtil.parseObj(o);
            users.add(new User(o1.getLong("g"),o1.getLong("m")));
        }

        for (Object o : JSONUtil.parseArray(s.getByGroup("wives", "wives")).toArray()) {
            JSONObject o1 = JSONUtil.parseObj(o);
            if(o1.keySet() == null)
                continue;

            UserWives w = new UserWives();
            wives.add(w);
            for(String key : o1.keySet()){
                w.put(key, o1.getInt(key));
            }
        }
    }

    private void save(){
        s.setByGroup("users","users", listToJson(users));
        s.setByGroup("wives","wives", listToJson(wives));
        s.store();
    }

    public static final String API = "https://h5.48.cn/resource/jsonp/allmembers.php?gid=00";
    public int downloadStarData(){
        this.starData.clear();

        String j = HttpRequest.get("https://h5.48.cn/resource/jsonp/allmembers.php?gid=00")
                .header("Host","h5.48.cn").header("Connection","keep-alive").header("sec-ch-ua","\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"").header("sec-ch-ua-mobile","?0").header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36").header("sec-ch-ua-platform","\"Windows\"").header("Accept","*/*").header("Sec-Fetch-Site","cross-site").header("Sec-Fetch-Mode","no-cors").header("Sec-Fetch-Dest","script").header("Referer","https://www.snh48.com/").header("Accept-Encoding","gzip, deflate, br").header("Accept-Language","zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-TW;q=0.6")
                .execute().body();

        int count = 0;
        for(Object j1 : JSONUtil.parseObj(j).getJSONArray("rows").toArray(new Object[0])){
            count++;
            JSONObject o1 = JSONUtil.parseObj(j1);
            JSONObject o = new JSONObject();
            o.set("s",o1.getStr("sname"));
            o.set("sid",o1.getStr("sid"));
            o.set("n",o1.getStr("nickname"));
            o.set("g",o1.getStr("gname")); //团
            o.set("t",o1.getStr("tname")); //队伍
            o.set("p",o1.getStr("pname")); //期数
            o.set("i",o1.getStr("pocket_id"));
            this.starData.add(o.toString());
        }

        FileUtil.writeString(new JSONArray(starData).toString(),starDataFile,"UTF-8");
        return count;
    }

    public String[] getSysLottery() {
        return sysLottery;
    }

    public String[] getSysData() {
        return sysData;
    }

    private String listToJson(List l){
        String out = "[";
        for(Object o : l){
            out+=o.toString()+",";
        }
        if(out.length()>1)
            return out.substring(0,out.length()-1)+"]";
        return "[]";
    }

    public int getUserIndex(long group, long member){
        for(int i = 0;i<this.users.size();i++){
            User user = this.users.get(i);
            if(user.g == group && user.m == member)
                return i;
        }
        return -1;
    }

    public void addNewWive(long group, long member, String wife){
        if(getUserIndex(group,member) == -1){
            users.add(new User(group,member));
            wives.add(new UserWives());
        }

        wives.get(getUserIndex(group,member)).add(wife);
        save();
    }

    public UserWives getUserWives(long group, long member){
        if(getUserIndex(group,member) == -1){
            users.add(new User(group,member));
            wives.add(new UserWives());
            save();
        }

        return wives.get(getUserIndex(group,member));
    }

    public ArrayList<String> getStarData() {
        return starData;
    }
}
