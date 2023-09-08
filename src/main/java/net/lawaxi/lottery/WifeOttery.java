package net.lawaxi.lottery;

import cn.hutool.cron.Scheduler;
import net.lawaxi.lottery.handler.Listener;
import net.lawaxi.lottery.handler.WifeHandler;
import net.lawaxi.lottery.handler.config;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class WifeOttery extends JavaPlugin {
    public static final WifeOttery INSTANCE = new WifeOttery();
    public static config config;

    private WifeOttery() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.wifeOttery", "0.1.8")
                .name("来个老婆")
                .author("小d")
                .build());
    }

    @Override
    public void onEnable() {
        config = new config(resolveConfigFile("config.setting"));
        GlobalEventChannel.INSTANCE.registerListenerHost(new Listener());
        listenBroadcast();
    }

    private void listenBroadcast() {
        Scheduler scheduler = new Scheduler();
        scheduler.schedule("0 0 8 * * *", new Runnable() { //每天八点重置
                    @Override
                    public void run() {
                        WifeHandler.INSTANCE.reset();
                    }
                }
        );
        scheduler.start();
    }
}