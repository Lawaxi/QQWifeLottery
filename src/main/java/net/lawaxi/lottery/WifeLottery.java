package net.lawaxi.lottery;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.cron.Scheduler;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.lottery.handler.BroadcastSender;
import net.lawaxi.lottery.handler.WifeHandler;
import net.lawaxi.lottery.manager.Listener;
import net.lawaxi.lottery.manager.MyCommands;
import net.lawaxi.lottery.manager.config;
import net.lawaxi.lottery.manager.database;
import net.lawaxi.lottery.utils.ImageModifier;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class WifeLottery extends JavaPlugin {
    public static final WifeLottery INSTANCE = new WifeLottery();
    private config config;

    private WifeLottery() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.wifeLottery48", "0.2.0-test4")
                .name("来个老婆48成员版")
                .author("小d")
                .build());
    }

    @Override
    public void onEnable() {
        config = new config(resolveConfigFile("config.setting"));
        database database = new database(config.getDatabaseFile());
        MyCommands.setDatabase(database);

        if (config.allowed()) {
            GlobalEventChannel.INSTANCE.registerListenerHost(new Listener(config));
            new WifeHandler(config, database);
        }
        if (config.allowedBroadcast()) {
            ImageModifier.setConfig(config);
            BroadcastSender.setConfig(config);
        }
        if (config.allowed() || config.allowedBroadcast()) {
            listenBroadcast();
        }

        CommandManager.INSTANCE.registerCommand(new MyCommands(), false);
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
        if (config.allowedBroadcast()) {
            FastDateFormat format = FastDateFormat.getInstance("MM.dd");
            scheduler.schedule("0 0 * * *", new Runnable() {
                        @Override
                        public void run() {
                            String today = format.format(new DateTime());
                            getLogger().info(today);
                            for (Object object : config.getStarData()) {
                                JSONObject o = JSONUtil.parseObj(object);
                                if (today.equals(o.getStr("birthday"))) {
                                    new BroadcastSender(o).start();
                                }
                            }
                        }
                    }
            );
        }
        scheduler.start();
    }
}