package net.lawaxi.lottery.handler;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.lottery.manager.config;
import net.lawaxi.lottery.manager.database;
import net.lawaxi.lottery.models.UserMaxSenseReport;
import net.lawaxi.lottery.models.UserWifeReport;
import net.lawaxi.lottery.models.Wish;
import net.lawaxi.lottery.utils.Chance;
import net.lawaxi.lottery.utils.WifeUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class WifeHandler {
    public static final String APIImage = "https://www.snh48.com/images/member/zp_%s.jpg";
    public static WifeHandler INSTANCE;
    private final config config;
    private final database database;
    //临时储存
    private final HashMap<Integer, Long> lastTime = new HashMap<>();
    private final List<Long> groupUpdate = new ArrayList<>(); //群每日第一
    //wish
    private final HashMap<Integer, String> lastWishTarget = new HashMap<>();
    //birthday
    private final ArrayList<Integer> starInBirthday = new ArrayList<>();

    public WifeHandler(net.lawaxi.lottery.manager.config config, database database) {
        INSTANCE = this;
        this.config = config;
        this.database = database;
    }

    public void testLaiGeLaoPo(String message, Member sender, Group group) {
        if (message.equalsIgnoreCase("update_star_data")) {
            group.sendMessage(new At(sender.getId()).plus(config.downloadStarData()));
            return;
        }

        if (message.equals("1")) {
            int user_id = getUserIndex(sender.getId(), group.getId());
            if (lastWishTarget.containsKey(user_id)) {
                wish(user_id, lastWishTarget.get(user_id), sender.getId(), group);
            }
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

        for (String o : config.getSysWish()) {
            if (message.startsWith(o)) {
                try {
                    String target = message.substring(message.indexOf(" ") + 1);
                    for (Object star : config.getStarData()) {
                        if (target.equalsIgnoreCase(JSONUtil.parseObj(star).getStr("s"))) {
                            int user_id = getUserIndex(sender.getId(), group.getId());
                            wish(user_id, target, sender.getId(), group);
                            return;
                        }
                    }
                    group.sendMessage(new At(sender.getId()).plus("\n许愿对象不存在"));

                } catch (IndexOutOfBoundsException e) {
                    group.sendMessage(new At(sender.getId()).plus("\n许愿格式错误，例：" + o + " 林忆宁"));
                }
                return;
            }
        }

        for (String o : config.getSysRank()) {
            if (message.equals(o)) {
                rank(sender.getId(), group);
                return;
            }
        }

        for (String o : config.getSysMyId()) {
            if (message.equals(o)) {
                int user_id = getUserIndex(sender.getId(), group.getId());
                group.sendMessage(new At(sender.getId()).plus("\nuid: " + String.format("%05d", user_id)));
                return;
            }
        }
    }

    public boolean wish(int id, String target, long sender, Group group) {
        try {
            if (!Wish.contains(id)) {
                new Wish(id, target);
                lastWishTarget.remove(id);
                group.sendMessage(new At(sender).plus("\n许愿成功：" + target));
                return true;
            } else {
                group.sendMessage(new At(sender).plus("\n您有许愿正在进行，" +
                        Wish.get(id).getTimeLast() + "次抽奖未抽中或任何一次抽中后接受新的许愿"));
            }
        } catch (Exception e) {
            group.sendMessage(new At(sender).plus("\n许愿功能载入错误"));
            e.printStackTrace();
        }
        return false;
    }

    public void laiGeLaoPo(long sender, Group group) {
        int user_id = getUserIndex(sender, group.getId());

        /* 资格判断 */
        int chance = Chance.getUserChance(user_id);
        boolean first = false;
        boolean useChance = false;
        if (lastTime.containsKey(user_id)) {
            long between = new Date().getTime() - lastTime.get(user_id);
            if (between < DateUnit.HOUR.getMillis() * 2) {
                //特殊次数
                if (chance > 0 && Chance.reduce(user_id) != -1) {
                    chance -= 1;
                    useChance = true;
                } else {
                    group.sendMessage(new At(sender).plus(WifeUtil.getChangingTimeBet(lastTime.get(user_id))));
                    return;
                }
            } else {
                lastTime.put(user_id, new Date().getTime());
            }

        } else {
            if (!groupUpdate.contains(group.getId())) {
                groupUpdate.add(group.getId());
                database.addCoins(user_id, 20, 1, "每日第一！");
                first = true;
            }
            lastTime.put(user_id, DateUtil.offsetHour(new Date(), -2).getTime());
        }

        /* 抽奖 */
        //抽老婆
        JSONObject mem = JSONUtil.parseObj(RandomUtil.randomEle(config.getStarData()));
        int sid = Integer.valueOf(mem.getStr("sid"));
        String name = mem.getStr("s");

        //抽情愫
        JSONObject max_sense = database.getMaxSenseRecord(sid, group.getId());
        int hq = max_sense == null ? 0 : max_sense.getInt("sense");
        int q = RandomUtil.randomInt(1, hq < 100 ? 101 : hq + 11);
        if (q > 79)
            database.addCoins(user_id, 1, 4, "带走老婆");
        String w = "";
        boolean wi = false;
        if (Wish.contains(user_id)) {
            String target = Wish.get(user_id).getTarget();
            wi = Wish.get(user_id).match(name);
            if (wi) {
                database.addCoins(user_id, 20, 2, "许愿成功");
                lastWishTarget.put(user_id, target);
                w = "\n许愿成功：本次情愫 " + q + "% 增加为 " + (q += 10) + "%！coins+20. 扣1继续相同的许愿。";
            } else {
                int timeLast = Wish.get(user_id).reduce();
                if (timeLast == 0)
                    lastWishTarget.put(user_id, target);
                w = timeLast == 0 ? "\n许愿 " + target + " 失败，您可以重新许愿。扣1继续相同的许愿。" : "\n当前许愿 " + target + " 剩余 " + timeLast + " 次。";
            }
        }

        database.appendLotteryRecord(group.getId(), database.getUserIdByNumbers(group.getId(), sender), sid, name, q, wi);

        /* 输出 */
        String dg = mem.getStr("g");
        String dt = mem.getStr("t");
        Message m = new At(sender)
                .plus((first ? "本日第一！coins+20\n" : "")
                        + " 今日老婆："
                        + (getGroupName(dg).equalsIgnoreCase(dt) ? getGroupName(dg) : getGroupName(dg) + " Team " + dt)//对于BEJ48/CKG48/IDFT的调整
                        + " " + name + " (" + mem.getStr("n") + ") (" + mem.getStr("p") + ")"
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

        //情愫王
        NormalMember senseFrom;
        if (q > hq) {
            hq = q;
            senseFrom = group.get(sender);
        } else {
            JSONObject max_sense_from = database.getUserDetailsById(max_sense.getInt("user_id"));
            if (max_sense_from != null) {
                senseFrom = group.get(max_sense_from.getLong("user_number"));
            } else {
                senseFrom = null;
            }
        }

        //生日
        boolean birthday = starInBirthday.contains(sid);
        if (birthday) {
            database.addCoins(user_id, 20, 3, "在成员生日时抽中");
        }

        group.sendMessage(m.plus(
                (mem.getStr("i", "0").equals("0") ? "" : "\n口袋ID: " + mem.getStr("i")) + "\n"
                        + WifeUtil.getChangingTime(lastTime.get(user_id))
                        + ((chance == 0 && !useChance) ? "" : "\n" + (useChance ? "使用 1 次，剩余" : "") + "可用抽奖次数 " + chance + " 次")
                        + "\n当前情愫王：" + (senseFrom == null ? "已退群成员" : (senseFrom.getNameCard().equals("") ? senseFrom.getNick() : senseFrom.getNameCard() + "(" + senseFrom.getNick() + ")")) + " [" + hq + "%]"
                        + w
                        + (birthday ? "\n生日当天抽中成员，coins+20" : "")));
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
        int user_id = getUserIndex(sender, group.getId());
        String out = "\nuid: " + String.format("%05d", user_id);

        //UserWifeReport
        UserWifeReport report = new UserWifeReport(database.getAllRecordsByUserId(user_id));
        if (report.getTotalBring() == 0) {
            out += "\n共抽 " + report.getTotal() + " 次\n你还没有老婆~ 情愫达到80%才可以带走捏";
        } else {
            out += "\n共抽 " + report.getTotal() + " 次，累计带走 " + report.getTotalBring() + " 人"
                    + "\n最高情愫：" + report.getWivesSortBySense().get(0).name + "(" + report.getWivesSortBySense().get(0).sense + "%) " + report.getWivesSortBySense().get(0).count + " 次"
                    + "\n最多次数：" + report.getWives().get(0).name + "(" + report.getWives().get(0).sense + "%) " + report.getWives().get(0).count + " 次";
        }

        //UserMaxSenseReport
        out += "\n---------------------------\n";
        UserMaxSenseReport maxSenseReport = new UserMaxSenseReport(database.getAllRecordsThatMaxSense(user_id, group.getId()));
        if (maxSenseReport.getTotal() == 0) {
            out += "您不是任何小偶像的情愫王";
        } else {
            out += "您是 " + maxSenseReport.getTotal() + " 位小偶像的情愫王：\n"
                    + String.join(" | ", maxSenseReport.getWives().stream()
                    .limit(3)
                    .map(rep -> rep.name + "(" + rep.sense + "%)")
                    .collect(Collectors.toList()));
        }

        group.sendMessage(new At(sender).plus(out));
    }

    public void rank(long sender, Group group) {
        int user_id = getUserIndex(sender, group.getId());
        JSONObject[] records = database.analyseGroupRecords(group.getId());

        if (records.length < 1) {
            group.sendMessage(new At(sender).plus("本群还没有成功带走老婆的用户"));
        }

        int max_count = records[0].getInt("count");
        List<String> resultList = Arrays.stream(records)
                .limit(5)
                .map(obj -> getRankLine(obj.getInt("user_id"), obj.getInt("count"), max_count))
                .collect(Collectors.toList());

        for (int i = 0; i < records.length; i++) {
            JSONObject obj = records[i];
            if (user_id == obj.getInt("user_id")) {
                String rt = "您的排名：1";
                if (i != 0) {
                    for (int j = i - 1; j >= 0; j--) {
                        JSONObject fobj = records[j];
                        int bet = fobj.getInt("count") - obj.getInt("count");
                        if (bet != 0) {
                            rt = "您的排名：" + (j + 2) + "，距离上一名差(" + bet + ")";
                            break;
                        }
                    }
                }

                group.sendMessage(new At(sender).plus("本群带走老婆次数排名/uid: \n"
                        + String.join("\n", resultList)
                        + "\n---------------------------\n"
                        + rt + "\n"
                        + getRankLine(user_id, obj.getInt("count"), max_count)));
                return;
            }
        }

        group.sendMessage(new At(sender).plus("本群带走老婆次数排名/uid: \n"
                + String.join("\n", resultList)
                + "\n---------------------------\n您还没有带走过老婆"));
    }

    private String getRankLine(int id, int count, int max_count) {
        int bars = Math.min(Math.round(count * 10.0f / max_count), count);
        String barsString;
        if (bars == 0 && count > 0)
            barsString = "▍";
        else
            barsString = new String(new char[bars]).replace('\0', '▉');
        return String.format("%d: %s(%d)", id, barsString, count);
    }

    public void reset() {
        lastTime.clear();
        groupUpdate.clear();
    }

    public void reset(int id) {
        lastTime.remove(id);
    }

    public void resetStarInBirthdayList() {
        this.starInBirthday.clear();
    }

    public boolean isInBirthday(JSONObject star, String today) {
        if (today.equals(star.getStr("birthday"))) {
            this.starInBirthday.add(Integer.valueOf(star.getStr("sid")));
            return true;
        } else {
            return false;
        }
    }

    public int getUserIndex(long sender, long group) {
        return database.getUserIdByNumbers(group, sender);
    }

    //接口使用，可配合集资插件
    public int offsetUserChance(int user_id, int offset) {
        return Chance.add(user_id, offset);
    }

    public void offsetCoins(int user_id, int offset, int reason_code, String reason) {
        database.addCoins(user_id, offset, reason_code, reason);
    }
}
