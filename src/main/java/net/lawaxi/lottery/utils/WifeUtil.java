package net.lawaxi.lottery.utils;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Calendar;
import java.util.Date;

public class WifeUtil {
    public static String recommend(int qingSu) {
        if (qingSu > 99)
            return "原地结婚";
        if (qingSu > 89)
            return "最佳拍档";
        if (qingSu > 79)
            return "带回家吧";
        if (qingSu > 69)
            return "约会去吧";
        if (qingSu > 19)
            return "再努努力";
        return "下辈子吧";
    }

    public static String getChangingTimeBet(long lastTime) {
        long lef = DateUnit.HOUR.getMillis() * 2 - new Date().getTime() + lastTime;
        if (lef < DateUnit.SECOND.getMillis())
            return "当前可更换";

        long hour = Long.valueOf(lef / DateUnit.HOUR.getMillis());
        lef -= DateUnit.HOUR.getMillis() * hour;
        long min = Long.valueOf(lef / DateUnit.MINUTE.getMillis());
        lef -= DateUnit.MINUTE.getMillis() * min;
        long sec = Long.valueOf(lef / DateUnit.SECOND.getMillis());
        return hour + "小时" + min + "分钟" + sec + "秒后可更换";
    }

    public static String getChangingTime(long lastTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 8) {
            return "请等待本日早8点CD刷新";
        }

        Date nextTime = DateUtil.offsetHour(new Date(lastTime), 2);
        if (new Date().getTime() - nextTime.getTime() >= 0)
            return "当前可更换";
        else
            return "下次更换时间：" + DateUtil.format(nextTime, "HH点mm分ss秒");
    }
}
