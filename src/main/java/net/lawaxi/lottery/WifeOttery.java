package net.lawaxi.lottery;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.cron.Scheduler;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.lottery.handler.BroadcastSender;
import net.lawaxi.lottery.handler.Listener;
import net.lawaxi.lottery.handler.WifeHandler;
import net.lawaxi.lottery.handler.config;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class WifeOttery extends JavaPlugin {
    public static final WifeOttery INSTANCE = new WifeOttery();
    public static net.lawaxi.lottery.handler.config config;

    private WifeOttery() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.wifeOttery48", "0.1.9-test3")
                .name("来个老婆48成员版")
                .author("小d")
                .build());
    }

    @Override
    public void onEnable() {
        config = new config(resolveConfigFile("config.setting"),
                resolveConfigFile("star_data"));
        if (config.allowed()) {
            GlobalEventChannel.INSTANCE.registerListenerHost(new Listener());
            new WifeHandler();
        }
        listenBroadcast();
    }

    private void listenBroadcast() {
        if (config.allowed() || config.allowedBroadcast()) {
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
}