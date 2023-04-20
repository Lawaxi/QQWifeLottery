package net.lawaxi.lottery;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class WifeOttery extends JavaPlugin {
    public static final WifeOttery INSTANCE = new WifeOttery();
    public static config config;

    private WifeOttery() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.wifeOttery48", "0.1.6-test4")
                .name("来个老婆48成员版")
                .author("小d")
                .build());
    }

    @Override
    public void onEnable() {
        config = new config(resolveConfigFile("config.setting"),
                resolveConfigFile("star_data"));
        GlobalEventChannel.INSTANCE.registerListenerHost(new Listener());
    }
}