package net.lawaxi.lottery.handler;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.lottery.models.User;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class WifeHandler {
    public static final String APIImage = "https://www.snh48.com/images/member/zp_%s.jpg";
    public static WifeHandler INSTANCE;
    private final config config;
    private final HashMap<Integer, Long> lastTime = new HashMap<>();
    private final HashMap<Integer, Integer> chances = new HashMap<>();

    public WifeHandler(config config) {
        this.config = config;
        INSTANCE = this;
    }

    public void testLaiGeLaoPo(String message, Member sender, Group group) {
        if (message.equalsIgnoreCase("update_star_data")) {
            group.sendMessage(new At(sender.getId()).plus("已更新成员列表，当前数据总数 " + config.downloadStarData() + " 人"));
            return;
        }

        if (message.equals("我的编号")) {
            group.sendMessage(new At(sender.getId()).plus("" + new User(group.getId(), sender.getId()).getIndex()));
            return;
        }

        for (String o : config.getSysLottery()) {
            if (message.equals(o)) {
                laiGeLaoPo(sender.getId(), group);
                return;
            }
        }

        for (String o : config.getSysData()) {
            if (message.equals(o)) {
                woDeLaoPo(sender.getId(), group);
                return;
            }
        }
    }

    public void laiGeLaoPo(long sender, Group group) {
        User user = new User(group.getId(), sender);
        int index = user.getIndex();
        int chance = chances.getOrDefault(index, 0);

        //免费次数
        if (lastTime.containsKey(index)) {
            long between = new Date().getTime() - lastTime.get(index);
            if (between < DateUnit.HOUR.getMillis() * 2) {
                //特殊次数
                if (chance > 0) {
                    chance -= 1;
                    chances.put(index, chance);
                } else {
                    group.sendMessage(new At(sender).plus(WifeUtil.getChangingTime(new Date(lastTime.get(index)))));
                    return;
                }
            } else {
                lastTime.put(index, new Date().getTime());
            }

        } else {
            lastTime.put(index, DateUtil.offsetHour(new Date(), -2).getTime());
        }

        JSONObject mem = JSONUtil.parseObj(RandomUtil.randomEle(config.getStarData()));
        int sid = Integer.valueOf(mem.getStr("sid"));
        int hq = config.getHistorySense(sid, group.getId());
        int q = RandomUtil.randomInt(1, hq < 100 ? 101 : hq + 11);
        if (q > hq) {
            config.testWifeModel(sid, user, q);
        }
        if (q > 79) {
            config.addNewWive(user, mem.getStr("s"));
        } else if (q > hq) {
            config.save();
        }

        String dg = mem.getStr("g");
        String dt = mem.getStr("t");
        Message m = new At(sender)
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

        Wife she = config.getWifeModel(Integer.valueOf(mem.getStr("sid")));
        NormalMember senseFrom = group.get(she.getSenseFromInGroup(group.getId()));
        group.sendMessage(m.plus(
                (mem.getStr("i", "0").equals("0") ? "" : "\n口袋ID: " + mem.getStr("i")) + "\n"
                        + WifeUtil.getChangingTime(new Date().getTime() - lastTime.get(index))
                        + "\n可用特殊次数 " + chance + " 次"
                        + "\n当前情愫王：" + (senseFrom == null ? "已退群成员" : (senseFrom.getNameCard().equals("") ? senseFrom.getNick() : senseFrom.getNameCard() + "(" + senseFrom.getNick() + ")")) + " [" + she.getSenseInGroup(group.getId()) + "%]"));
    }

    private InputStream getRes(String resLoc) {
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

    public void woDeLaoPo(long sender, Group group) {
        UserWifeReport report = new UserWifeReport(config.getUserWives(new User(group.getId(), sender)));
        if (report.getWifeTotal() == 0) {
            group.sendMessage(new At(sender).plus("\n你还没有老婆~ 情愫达到80%才可以带走捏"));
            return;
        }

        //带走次数御三
        String yuSan = "";
        for (int i = 0; i < (report.getWifeTotal() > 3 ? 3 : report.getWifeTotal()); i++) {
            String wife = report.getWives().get(i);
            yuSan += wife + " " + report.getCount(wife) + "次" + " | ";
        }

        //带走情愫（以情愫王的方式）御三
        String qingSuWang = "您不是任何小偶像的情愫王" + " | ";
        ArrayList<Wife> wangs = new ArrayList<>();
        for (Wife wife : config.getWiveModels()) {
            if (wife.isSenseFrom(group.getId(), sender)) {
                wangs.add(wife);
            }
        }
        if (wangs.size() > 0) {
            wangs.sort((wa, wb) -> wb.getSenseInGroup(group.getId()) - wa.getSenseInGroup(group.getId()));
            qingSuWang = "您是 " + wangs.size() + " 位小偶像的情愫王：\n";

            for (int i = 0; i < (wangs.size() > 3 ? 3 : wangs.size()); i++) {
                JSONObject wife = config.getStarBySid("" + wangs.get(i).sid);
                if (wife != null) {
                    qingSuWang += wife.getStr("s", "未知");
                } else {
                    qingSuWang += "未知"; //处理成员被移出名单（毕业）的动态过程
                }
                qingSuWang += " " + wangs.get(i).getSenseInGroup(group.getId()) + "% | ";
            }
            /*
            //若不考虑被移出名单的成员，可以考虑下面的方法
            int count = 0;
            for (int i = 0; i < wangs.size() && count < 3; i++) {
                JSONObject wife = config.getStarBySid("" + wangs.get(i).sid);
                if (wife != null) {
                    count++;
                    qingSuWang += wife.getStr("s", "未知") + " " + wangs.get(i).getSenseInGroup(group.getId()) + "% | ";
                }
            }*/
        }

        group.sendMessage(new At(sender).plus(
                "\n累计带走" + report.getWifeTotal() + "人 共" + report.getTotal() + "次\n"
                        + "带走次数御三：\n" + yuSan.substring(0, yuSan.length() - 3)
                        + "\n---------\n"
                        + qingSuWang.substring(0, qingSuWang.length() - 3)));
    }

    public void reset() {
        lastTime.clear();
    }

    public int getUserIndex(long sender, long group) {
        return new User(group, sender).getIndex();
    }

    public User getUserByIndex(int index) {
        return config.getUserByIndex(index);
    }

    public void offsetUserChance(int index, int offset) {
        chances.put(index, chances.get(index) + offset);
    }

}
