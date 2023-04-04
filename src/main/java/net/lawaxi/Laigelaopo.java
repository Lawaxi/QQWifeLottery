package net.lawaxi;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class Laigelaopo extends JavaPlugin {
    public static final Laigelaopo INSTANCE = new Laigelaopo();
    public static config config;

    private Laigelaopo() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.laigelaopo", "0.1.0")
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