package net.lawaxi.lottery.handler;

import cn.hutool.json.JSONObject;
import net.lawaxi.lottery.manager.config;
import net.lawaxi.lottery.utils.ImageModifier;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;
import java.io.InputStream;

public class BroadcastSender extends Thread {
    private static net.lawaxi.lottery.manager.config config;
    private final JSONObject star;

    public BroadcastSender(JSONObject star) {
        this.star = star;
    }

    public static void setConfig(config config) {
        BroadcastSender.config = config;
    }

    @Override
    public void run() {
        InputStream i = null;
        try {
            i = ImageModifier.modifyImage(star.getStr("s"), WifeHandler.INSTANCE.getImage(star));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Bot b : Bot.getInstances()) {
            for (String group : config.getBirthdayBroadcastGroup()) {
                Group g = b.getGroup(Long.valueOf(group));
                if (g != null) {
                    Message a = new PlainText("生日快乐，" + star.getStr("s") + "！");
                    if (i != null) {
                        try {
                            a = a.plus(g.uploadImage(ExternalResource.create(i)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    g.sendMessage(a);
                }
            }
        }
    }
}
