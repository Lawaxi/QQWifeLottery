package net.lawaxi.lottery;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class WifeOttery extends JavaPlugin {
    public static final WifeOttery INSTANCE = new WifeOttery();
    public static config config;

    private WifeOttery() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.wifeOttery", "0.1.4-test2")
                .name("来个老婆")
                .author("小d")
                .build());
    }

    @Override
    public void onEnable() {
        this.config = new config(resolveConfigFile("config.setting"));
        GlobalEventChannel.INSTANCE.registerListenerHost(new Listener());
    }
}