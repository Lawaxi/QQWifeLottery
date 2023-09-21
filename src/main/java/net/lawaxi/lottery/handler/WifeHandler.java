package net.lawaxi.lottery.handler;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.lottery.WifeOttery;
import net.lawaxi.lottery.models.UserWifeReport;
import net.lawaxi.lottery.models.Wife;
import net.lawaxi.lottery.utils.WifeUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

public class WifeHandler {
    public static final String APIImage = "https://www.snh48.com/images/member/zp_%s.jpg";
    public static WifeHandler INSTANCE;
    private final HashMap<Long, HashMap<Integer, Long>> lastTime = new HashMap<>();

    public WifeHandler() {
        INSTANCE = this;
    }

    public void testLaiGeLaoPo(String message, Member sender, Group group) {
        for (String o : WifeOttery.config.getSysLottery()) {
            if (message.equals(o)) {
                laiGeLaoPo(sender, group);
                return;
            }
        }

        for (String o : WifeOttery.config.getSysData()) {
            if (message.equals(o)) {
                woDeLaoPo(sender, group);
                return;
            }
        }
    }

    private void laiGeLaoPo(Member sender, Group group) {
        if (!lastTime.containsKey(group.getId()))
            lastTime.put(group.getId(), new HashMap<>());

        HashMap glt = lastTime.get(group.getId());
        if (glt.containsKey(sender.getId())) {
            Date date = (Date) glt.get(sender.getId());
            long between = new Date().getTime() - date.getTime();
            if (between < DateUnit.HOUR.getMillis() * 2) {
                group.sendMessage(new At(sender.getId()).plus(WifeUtil.getChangingTime(date) + "，再等等吧"));
                return;
            }
        }

        JSONObject mem = JSONUtil.parseObj(RandomUtil.randomEle(WifeOttery.config.getStarData()));
        glt.put(sender.getId(), glt.containsKey(sender.getId()) ? new Date() : DateUtil.offsetHour(new Date(), -2));//是否为换老婆

        int sid = Integer.valueOf(mem.getStr("sid"));
        int hq = WifeOttery.config.getHistorySense(sid, group.getId());
        int q = RandomUtil.randomInt(1, hq < 100 ? 101 : hq + 11);
        if (q > hq) {
            WifeOttery.config.testWifeModel(sid, group.getId(), sender.getId(), q);
        }
        if (q > 79) {
            WifeOttery.config.addNewWive(group.getId(), sender.getId(), mem.getStr("s"));
        } else if (q > hq) {
            WifeOttery.config.save();
        }

        String dg = mem.getStr("g");
        String dt = mem.getStr("t");
        Message m = new At(sender.getId())
                .plus(" 今日老婆："
                        + (getGroupName(dg).equalsIgnoreCase(dt) ? getGroupName(dg) : getGroupName(dg) + " Team " + dt)//对于BEJ48/CKG48/IDFT的调整
                        + " " + mem.getStr("s") + " (" + mem.getStr("n") + ") (" + mem.getStr("p") + ")"
                        + " | 情愫："
                        + q
                        + "% "
                        + WifeUtil.recommend(q));
        //大头照
        try {
            m = m.plus(group.uploadImage(ExternalResource.create(getImage(mem))));
        } catch (Exception e) {
            //没有图片或上传问题
        }

        Wife she = WifeOttery.config.getWifeModel(Integer.valueOf(mem.getStr("sid")));
        NormalMember senseFrom = group.get(she.getSenseFromInGroup(group.getId()));
        group.sendMessage(m.plus(
                (mem.getStr("i", "0").equals("0") ? "" : "\n口袋ID: " + mem.getStr("i")) + "\n"
                        + WifeUtil.getChangingTime((Date) glt.get(sender.getId()))
                        + "\n当前情愫王：" + (senseFrom == null ? "已退群成员" : (senseFrom.getNameCard().equals("") ? senseFrom.getNick() : senseFrom.getNameCard() + "(" + senseFrom.getNick() + ")")) + " [" + she.getSenseInGroup(group.getId()) + "%]"));
    }

    public InputStream getRes(String resLoc) {
        return HttpRequest.get(resLoc).execute().bodyStream();
    }

    public InputStream getImage(JSONObject mem) {
        return getRes(String.format(APIImage, mem.getStr("sid")));
    }

    private String getGroupName(String pre) {
        switch (pre) {
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

    private void woDeLaoPo(Member sender, Group group) {
        UserWifeReport report = new UserWifeReport(WifeOttery.config.getUserWives(group.getId(), sender.getId()));
        if (report.getWifeTotal() == 0) {
            group.sendMessage(new At(sender.getId()).plus("\n你还没有老婆~ 情愫达到80%才可以带走捏"));
            return;
        }

        String yuSan = "";
        for (int i = 0; i < (report.getWifeTotal() > 3 ? 3 : report.getWifeTotal()); i++) {
            String wife = report.getWives().get(i);
            yuSan += wife + " " + report.getCount(wife) + "次" + " | ";
        }


        group.sendMessage(new At(sender.getId()).plus(
                "\n累计带走" + report.getWifeTotal() + "人 共" + report.getTotal() + "次\n"
                        + "带走次数御三：\n" + yuSan.substring(0, yuSan.length() - 3)));
    }

    public void reset() {
        for (HashMap m : lastTime.values()) {
            m.clear();
        }
    }
}
