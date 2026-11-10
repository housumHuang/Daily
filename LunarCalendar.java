import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 农历工具类:公历转农历,支持 1900-2049 年,供其他模块复用。
 * 使用经典农历数据表(LUNAR_INFO),每个元素(20位)按位编码一年的农历信息:
 *   位20(0x10000): 闰月是否有30天(1=30,0=29)
 *   位19~8       : 正月~腊月大小月(1=30天,0=29天)
 *   低4位(0x0F)  : 闰月是几月(0=无闰月)
 */
public final class LunarCalendar {

    private static final int[] LUNAR_INFO = {
        0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
        0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
        0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
        0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
        0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,
        0x06ca0,0x0b550,0x15355,0x04da0,0x0a5b0,0x14573,0x052b0,0x0a9a8,0x0e950,0x06aa0,
        0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,
        0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b6a0,0x195a6,
        0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,
        0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,
        0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,
        0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,
        0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,
        0x05aa0,0x076a3,0x096d0,0x04afb,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,
        0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0
    };

    private static final String[] GAN = {"甲","乙","丙","丁","戊","己","庚","辛","壬","癸"};
    private static final String[] ZHI = {"子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"};
    private static final String[] LUNAR_MONTH_NAMES = {"正月","二月","三月","四月","五月","六月","七月","八月","九月","十月","冬月","腊月"};
    private static final String[] LUNAR_DAY_NAMES = {"初一","初二","初三","初四","初五","初六","初七","初八","初九","初十",
        "十一","十二","十三","十四","十五","十六","十七","十八","十九","二十",
        "廿一","廿二","廿三","廿四","廿五","廿六","廿七","廿八","廿九","三十"};

    /** 工具类,禁止实例化 */
    private LunarCalendar() {}

    /** 农历日期(不可变) */
    public static final class LunarDate {
        public final int year;             // 农历年(数字)
        public final int month;            // 农历月(1-12)
        public final int day;              // 农历日(1-30)
        public final boolean leapMonth;    // 是否闰月

        LunarDate(int year, int month, int day, boolean leapMonth) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.leapMonth = leapMonth;
        }

        /** 干支纪年,如"丙午" */
        public String getGanzhiYear() {
            return GAN[(year - 4) % 10] + ZHI[(year - 4) % 12] + "年";
        }

        /** 农历月中文名,闰月带"闰"前缀,如"闰二月" */
        public String getChineseMonth() {
            return (leapMonth ? "闰" : "") + LUNAR_MONTH_NAMES[month - 1];
        }

        /** 农历日中文名,如"初五" */
        public String getChineseDay() {
            return LUNAR_DAY_NAMES[day - 1];
        }

        /** 形如: 丙午-闰二月-初一 */
        @Override
        public String toString() {
            return getGanzhiYear() + "-" + getChineseMonth() + "-" + getChineseDay();
        }
    }

    /** 公历日期 -> 农历日期 */
    public static LunarDate of(LocalDate date) {
        // 1900-01-31 为农历1900年正月初一
        long offset = ChronoUnit.DAYS.between(LocalDate.of(1900, 1, 31), date);
        int i, temp = 0;
        for (i = 1900; i < 2100 && offset > 0; i++) {
            temp = lunarYearDays(i);
            offset -= temp;
        }
        if (offset < 0) { offset += temp; i--; }
        int lunarYear = i;
        int leap = leapMonth(lunarYear);
        boolean isLeap = false;
        for (i = 1; i < 13 && offset > 0; i++) {
            if (leap > 0 && i == (leap + 1) && !isLeap) {
                --i; isLeap = true; temp = leapDays(lunarYear);
            } else {
                temp = monthDays(lunarYear, i);
            }
            if (isLeap && i == (leap + 1)) { isLeap = false; }
            offset -= temp;
        }
        if (offset == 0 && leap > 0 && i == leap + 1) {
            if (isLeap) { isLeap = false; } else { isLeap = true; --i; }
        }
        if (offset < 0) { offset += temp; --i; }
        return new LunarDate(lunarYear, i, (int) offset + 1, isLeap);
    }

    /** 公历年月日 -> 农历日期 */
    public static LunarDate of(int year, int month, int day) {
        return of(LocalDate.of(year, month, day));
    }

    // ---------- 内部计算 ----------

    private static int leapMonth(int y) { return LUNAR_INFO[y - 1900] & 0xf; }

    private static int leapDays(int y) {
        return leapMonth(y) != 0 ? ((LUNAR_INFO[y - 1900] & 0x10000) != 0 ? 30 : 29) : 0;
    }

    private static int monthDays(int y, int m) {
        return (LUNAR_INFO[y - 1900] & (0x10000 >> m)) != 0 ? 30 : 29;
    }

    private static int lunarYearDays(int y) {
        int sum = 348;
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            sum += (LUNAR_INFO[y - 1900] & i) != 0 ? 1 : 0;
        }
        return sum + leapDays(y);
    }
}
