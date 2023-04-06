package net.lawaxi.lottery;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.RandomUtil;
import net.lawaxi.lottery.models.UserWifeReport;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Listener  extends SimpleListenerHost {

    private HashMap<Long, HashMap<Integer,Long>> lastTime = new HashMap<>();

    @EventHandler()
    public ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Member sender = event.getSender();
        Group group = event.getGroup();
        String message = event.getMessage().contentToString();
        testLaiGeLaoPo(message,sender,group);

        return ListeningStatus.LISTENING;
    }

    private void testLaiGeLaoPo(String message, Member sender, Group group){
        for(String o : WifeOttery.INSTANCE.config.getSysOttery())
        {
            if(message.equals(o)) {
                laiGeLaoPo(sender, group);
                return;
            }
        }

        for(String o : WifeOttery.INSTANCE.config.getSysData())
        {
            if(message.equals(o)) {
                woDeLaoPo(sender, group);
                return;
            }
        }
    }

    private void laiGeLaoPo(Member sender, Group group){
        if(!lastTime.containsKey(group.getId()))
            lastTime.put(group.getId(),new HashMap<>());

        HashMap glt = lastTime.get(group.getId());
        if(glt.containsKey(sender.getId())){
            long between = new Date().getTime() - ((Date) glt.get(sender.getId())).getTime();
            if(between < DateUnit.HOUR.getMillis()*2){
                group.sendMessage(new At(sender.getId()).plus(util.getChangingTime(between)+"后可更换，在等等吧"));
                return;
            }
        }

        NormalMember mem = RandomUtil.randomEle(new ArrayList<>(group.getMembers().delegate));
        int q = RandomUtil.randomInt(1,100);
        glt.put(sender.getId(), new Date());
        if(q > 79)
            WifeOttery.config.addNewWive(group.getId(),sender.getId(),mem.getId());

        group.sendMessage(new At(sender.getId())
                .plus(" 今日老婆："
                        +(mem.getNameCard().equals("") ?  mem.getNick() : mem.getNameCard()+"("+mem.getNick()+")")
                        +" | 情愫："
                        +q
                        +"% "
                        +util.recommend(q)
                        +"\n"+util.getChangingTime(0l)+"后可更换"));
    }

    private void woDeLaoPo(Member sender, Group group){
        UserWifeReport report = new UserWifeReport(WifeOttery.config.getUserWives(group.getId(),sender.getId()));
        if(report.getWifeTotal()==0)
        {
            group.sendMessage(new At(sender.getId()).plus("\n你还没有老婆~ 情愫达到80%才可以带走捏"));
            return;
        }

        String yuSan = "";
        for(int i = 0;i<(report.getWifeTotal()>4 ? 4 : report.getWifeTotal());i++){
            long wife = report.getWives().get(i);
            NormalMember mem = group.get(wife);
            if(mem == null)
                yuSan+="已退群用户"+report.getCount(wife)+"次"+" | ";
            else
                yuSan+=(mem.getNameCard().equals("") ?  mem.getNick() : mem.getNameCard()+"("+mem.getNick()+")")+report.getCount(wife)+"次"+" | ";
        }


        group.sendMessage(new At(sender.getId()).plus(
                "\n累计带走"+report.getWifeTotal()+"人 共"+report.getTotal()+"次\n"
                        +"带走次数御三：\n"+yuSan.substring(0,yuSan.length()-3)));
    }
}
