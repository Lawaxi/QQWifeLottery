package net.lawaxi.lottery;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.cron.Scheduler;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.lottery.handler.BroadcastSender;
import net.lawaxi.lottery.handler.PasswordHandler;
import net.lawaxi.lottery.handler.WifeHandler;
import net.lawaxi.lottery.manager.Listener;
import net.lawaxi.lottery.manager.MyCommands;
import net.lawaxi.lottery.manager.config;
import net.lawaxi.lottery.manager.database;
import net.lawaxi.lottery.models.Chance;
import net.lawaxi.lottery.models.Wish;
import net.lawaxi.lottery.utils.ImageModifier;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class WifeLottery extends JavaPlugin {
    public static final WifeLottery INSTANCE = new WifeLottery();
    private config config;
    private WifeHandler wife;
    private PasswordHandler password;

    private WifeLottery() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.wifeLottery48", "0.2.0-test11")
                .name("来个老婆48成员版")
                .author("小d圆赐名")
                .build());
    }

    @Override
    public void onEnable() {
        config = new config(resolveConfigFile("config.setting"));
        database database = config.getDatabase();
        password = config.getPassword();
        MyCommands.setDatabase(database);

        if (config.allowed()) {
            GlobalEventChannel.INSTANCE.registerListenerHost(new Listener(config));
            Wish.initWishList(database);
            Chance.initChance(database);
            wife = new WifeHandler(config, database);
            listenBroadcast();
        }
        if (config.allowedBroadcast()) {
            ImageModifier.setConfig(config);
            BroadcastSender.setConfig(config);
        }

        CommandManager.INSTANCE.registerCommand(new MyCommands(), false);
        WifeLottery.INSTANCE.getLogger().info("提示：由0.1.9升级至0.2.0之后版本可以使用/wife48 migrate命令迁移数据，详见realises说明");
        WifeLottery.INSTANCE.getLogger().info("提示：0.2.0-test9以前创建的数据库id使用INTEGER，若需要更换为BIGINT，请使用/wife48 update");
    }

    private void listenBroadcast() {
        Scheduler scheduler = new Scheduler();

        if (config.allowed()) {
            scheduler.schedule("0 8 * * *", new Runnable() { //每天八点重置
                        @Override
                        public void run() {
                            WifeHandler.INSTANCE.reset();
                        }
                    }
            );
        }
        FastDateFormat format = FastDateFormat.getInstance("MM.dd");
        scheduler.schedule("0 0 * * *", new Runnable() {
                    @Override
                    public void run() {
                        String today = format.format(new DateTime());
                        wife.resetStarInBirthdayList();
                        getLogger().info(today);
                        for (Object star : config.getStarData()) {
                            JSONObject s = JSONUtil.parseObj(star);
                            if (wife.isInBirthday(s, today)) {
                                if (config.allowedBroadcast()) {
                                    new BroadcastSender(s).start();
                                }
                            }
                        }
                    }
                }
        );
        scheduler.start();
    }

    public PasswordHandler getPassword() {
        return password;
    }
}