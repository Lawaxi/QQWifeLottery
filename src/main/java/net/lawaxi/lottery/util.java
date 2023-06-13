package net.lawaxi.lottery;

import cn.hutool.core.date.DateUnit;

public class util {
    public static String recommend(int qingSu) {
        if (qingSu == 100)
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

    public static String getChangingTime(long bet) {
        long lef = DateUnit.HOUR.getMillis() * 2 - bet;
        long hour = Long.valueOf(lef / DateUnit.HOUR.getMillis());
        lef -= DateUnit.HOUR.getMillis() * hour;
        long min = Long.valueOf(lef / DateUnit.MINUTE.getMillis());
        lef -= DateUnit.MINUTE.getMillis() * min;
        long sec = Long.valueOf(lef / DateUnit.SECOND.getMillis());
        if (hour == min && min == sec && sec == 0)
            return "当前可更换";
        return hour + "小时" + min + "分钟" + sec + "秒后可更换";
    }
}
