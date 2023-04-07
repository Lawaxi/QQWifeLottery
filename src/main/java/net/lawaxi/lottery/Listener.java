package net.lawaxi.lottery;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.lottery.models.UserWifeReport;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;
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
        if(message.equals("update_star_data")){
            group.sendMessage(new At(sender.getId()).plus("已更新成员列表，当前数据总数 "+WifeOttery.config.downloadStarData()+" 人"));
        }

        return ListeningStatus.LISTENING;
    }

    private void testLaiGeLaoPo(String message, Member sender, Group group){
        for(String o : WifeOttery.INSTANCE.config.getSysLottery())
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

    public static final String APIImage = "https://www.snh48.com/images/member/zp_%s.jpg";
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


        JSONObject mem = JSONUtil.parseObj(RandomUtil.randomEle(WifeOttery.config.getStarData()));
        glt.put(sender.getId(), new Date());
        int q;
        if(RandomUtil.randomBoolean()) {
            q = RandomUtil.randomInt(80, 100);
            WifeOttery.config.addNewWive(group.getId(),sender.getId(),mem.getStr("s"));
        }else q = RandomUtil.randomInt(1,79);


        String dg = mem.getStr("g");
        String dt = mem.getStr("t");
        Message m = new At(sender.getId())
                .plus(" 今日老婆："
                        +(dg.equals(dt) ? getGroupName(dg) : getGroupName(dg)+" Team "+dt) +" " + mem.getStr("s") +" ("+ mem.getStr("n")+") ("+mem.getStr("p")+")"
                        +" | 情愫："
                        +q
                        +"% "
                        +util.recommend(q));
        //大头照
        try{
            m = m.plus(group.uploadImage(ExternalResource.create(getRes(String.format(APIImage,mem.getStr("sid"))))));
        }catch (Exception e){
            //没有图片或上传问题
        }

        group.sendMessage(m.plus(
                        "\n口袋ID: "+mem.getStr("i")+"\n"
                        +util.getChangingTime(0l)+"后可更换"));
    }



    public InputStream getRes(String resLoc) {
        return HttpRequest.get(resLoc).execute().bodyStream();
    }

    private String getGroupName(String pre){
        switch (pre){
            case "SNH":
                return "SNH48";
            case "GNZ":
                return "GNZ48";
            case "BEJ":
                return "BEJ48";
            case "CKG":
                return "CKG48";
            case "CGT":
                return "CGT48";
            default:
                return pre;
        }
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
            String wife = report.getWives().get(i);
            yuSan+=wife+" "+report.getCount(wife)+"次"+" | ";
        }


        group.sendMessage(new At(sender.getId()).plus(
                "\n累计带走"+report.getWifeTotal()+"人 共"+report.getTotal()+"次\n"
                        +"带走次数御三：\n"+yuSan.substring(0,yuSan.length()-3)));
    }
}
