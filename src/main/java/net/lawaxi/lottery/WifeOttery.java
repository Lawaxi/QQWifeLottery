package net.lawaxi.lottery;

import cn.hutool.cron.CronUtil;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;

public final class WifeOttery extends JavaPlugin {
    public static final WifeOttery INSTANCE = new WifeOttery();
    public static config config;

    private WifeOttery() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.wifeOttery", "0.1.7-test2")
                .name("来个老婆")
                .author("小d")
                .build());
    }

    @Override
    public void onEnable() {
        config = new config(resolveConfigFile("config.setting"));
        GlobalEventChannel.INSTANCE.registerListenerHost(new Listener());
        GlobalEventChannel.INSTANCE.parentScope(INSTANCE).subscribeOnce(BotOnlineEvent.class, event -> {
            listenBroadcast(event);
        });
    }

    private void listenBroadcast(BotOnlineEvent event) {
        CronUtil.schedule("0 0 8 * * *", new Runnable() { //每天八点重置
                    @Override
                    public void run() {
                        Listener.INSTANCE.reset();
                    }
                }
        );
    }
}