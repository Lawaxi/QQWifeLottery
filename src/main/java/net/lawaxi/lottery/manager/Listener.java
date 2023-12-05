package net.lawaxi.lottery.manager;

import net.lawaxi.lottery.WifeLottery;
import net.lawaxi.lottery.handler.WifeHandler;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;

public class Listener extends SimpleListenerHost {
    private final config config;

    public Listener(config config) {
        this.config = config;
    }

    @EventHandler()
    public ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Group group = event.getGroup();

        if (config.doesGroupAllowed(group.getId())) {
            WifeHandler.INSTANCE.testLaiGeLaoPo(
                    event.getMessage().contentToString(),
                    event.getSender(),
                    group);
        }

        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onUserMessage(UserMessageEvent event) {
        String m = event.getMessage().contentToString();
        if (m.startsWith("修改密码")) {
            WifeLottery.INSTANCE.getPassword().testChangePassword(m, event.getSender());
        }

        return ListeningStatus.LISTENING;
    }
}
