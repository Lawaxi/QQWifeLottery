package net.lawaxi.lottery.manager;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.lottery.WifeLottery;
import net.lawaxi.lottery.handler.WifeHandler;
import net.mamoe.mirai.console.command.java.JCompositeCommand;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class MyCommands extends JCompositeCommand {

    private static database database;
    private static net.lawaxi.lottery.manager.config config;

    public MyCommands() {
        super(WifeLottery.INSTANCE, "wife48");
    }

    public static void setDatabase(database database) {
        MyCommands.database = database;
    }

    public static void setConfig(config config) {
        MyCommands.config = config;
    }

    @SubCommand({"reset"})
    public void reset(int id) {
        WifeHandler.INSTANCE.reset(id);
        WifeLottery.INSTANCE.getLogger().info("重置等待时间成功。");
    }

    @SubCommand({"migrate"})
    public void migrate() {
        File formerConfig = new File(new File(WifeLottery.INSTANCE.getConfigFolder()
                .getParentFile(), "net.lawaxi.wifeOttery48"), "config.setting");
        Setting s = new Setting(formerConfig, StandardCharsets.UTF_8, false);
         /*
        JSONArray users = JSONUtil.parseArray(s.getStr("users", "users", "[]"));
        int between = -2;
        for(Object u : users){
            JSONObject user = JSONUtil.parseObj(u);
            int id = database.getUserIdByNumbers(user.getLong("g"), user.getLong("m"));
            if(between == -2){
                between = id - 1;
            }
        }

        JSONArray user_wives = JSONUtil.parseArray(s.getStr("wives", "users", "[]"));
        for(Object u : user_wives) {
            JSONObject user_wife = JSONUtil.parseObj(u);
            for (String key : user_wife.keySet()) {
                int count = user_wife.getInt(key);
            }
        }*/

        JSONObject wives = JSONUtil.parseObj(s.getStr("sense", "wives", "{}"));
        for (String key : wives.keySet()) {
            JSONObject wife = wives.getJSONObject(key);
            for (String group : wife.keySet()) {
                Long[] sense = (Long[]) wife.getJSONArray(group).stream().toArray();
                database.appendLotteryRecord(
                        Long.valueOf(group),
                        database.getUserIdByNumbers(Long.valueOf(group), sense[0]),
                        Integer.valueOf(key),
                        getStarName(key),
                        Integer.valueOf("" + sense[1]),
                        false
                );
            }
        }
        WifeLottery.INSTANCE.getLogger().info("迁移完成。");
    }


    @SubCommand({"update"})
    public void update() {
        if (database.isSQLite()) {
            WifeLottery.INSTANCE.getLogger().info("暂不支持SQLite使用BIGINT，请使用MySQL。");
        } else {
            String[] tables = new String[]{"logs", "users", "wish", "coin_log"};
            for (String table : tables)
                database.execute("ALTER TABLE " + table + " MODIFY COLUMN id BIGINT AUTO_INCREMENT");
            WifeLottery.INSTANCE.getLogger().info("升级完成。");
        }
    }

    private String getStarName(String sid) {
        for (Object s : config.starData) {
            JSONObject star = JSONUtil.parseObj(s);
            if (star.getStr("sid").equals(sid)) {
                return star.getStr("s");
            }
        }
        return "未知";
    }
}