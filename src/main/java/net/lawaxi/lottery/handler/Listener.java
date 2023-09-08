package net.lawaxi.lottery.handler;

import net.lawaxi.lottery.WifeOttery;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;

public class Listener extends SimpleListenerHost {

    @EventHandler()
    public ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Group group = event.getGroup();
        if (WifeOttery.config.doesGroupAllowed(group.getId())) {
            Member sender = event.getSender();
            String message = event.getMessage().contentToString();
            if (message.equals("update_star_data")) {
                group.sendMessage(new At(sender.getId()).plus("已更新成员列表，当前数据总数 " + WifeOttery.config.downloadStarData() + " 人"));
            } else {
                WifeHandler.INSTANCE.testLaiGeLaoPo(message, sender, group);
            }
        }

        return ListeningStatus.LISTENING;
    }
}
