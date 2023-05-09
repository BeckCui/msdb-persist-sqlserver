package com.dhl.fin.api.common.util;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Created by CuiJianbo on 2020.02.21.
 */
public class DateUtil {

    // 得到系统时间
    public static Date getSysDate() {
        return new Date();
    }

    public static Timestamp getSysTime() {
        return new Timestamp(getSysDate().getTime());
    }

    // 得到系统时间
    public static String getSysTimeByZN() {
        SimpleDateFormat znSdf = new SimpleDateFormat("yyyy年MM月dd日 E");
        return znSdf.format(getSysTime());
    }

    public static String getZN(Date date) {
        SimpleDateFormat znSdf = new SimpleDateFormat("yyyy年MM月dd日 E");
        return znSdf.format(date);
    }

    // 得到格式化字符串
    public static String getTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    // 得到格式化字符串
    public static String getTimeS(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(date);
    }

    public static String getTime(Serializable date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(Long.valueOf(date.toString())));
    }


    // 得到格式化字符串
    public static String getLongTime(Date date) {
        SimpleDateFormat longSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return longSdf.format(date);
    }

    public static String getLongTime(Serializable date) {
        SimpleDateFormat longSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return longSdf.format(new Date(Long.valueOf(date.toString())));
    }

    public static Date getData(Serializable date) {
        return new Date(Long.valueOf(date.toString()));
    }

    // 得到格式化字符串
    public static String getFullTime(Date date) {
        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fullSdf.format(date);
    }

    // 得到格式化字符串
    public static String getFullTimeS(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(date);
    }


    public static String getFullTime(Serializable date) {
        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fullSdf.format(new Date(Long.valueOf(date.toString())));
    }

    public static String getFullTime(Timestamp date) {
        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fullSdf.format(new Date(date.getTime()));
    }

    // 得到格式化字符串
    public static String getShortTime(Date date) {
        SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM");
        return shortSdf.format(date);
    }

    public static Date getShortDate(String stringDate) {
        try {
            SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM");
            return shortSdf.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 日期转日期对象
    public static Date getDate(String time) {
        try {
            if (StringUtils.isEmpty(time)) {
                return null;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 得到当前日期的0点
    public static Date getFirstDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // 得到当前日期的24点
    public static Date getLastDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // 得到日期所在月份第一天的0点
    public static Date getMonthFirstDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getFirstDate(calendar.getTime());
    }

    // 得到日期所在月份最后一天的24点
    public static Date getMonthLastDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DATE, 1);
        calendar.add(Calendar.DATE, -1);
        return getLastDate(calendar.getTime());
    }

    public static java.sql.Date getSqlDate(String time) {
        if (StringUtils.isEmpty(time)) {
            return null;
        }
        Date date = getDate(time);
        if (date != null) {
            return new java.sql.Date(date.getTime());
        }
        return null;
    }


    // 日期转日期对象
    public static Date getLongDate(String time) {
        try {
            SimpleDateFormat longSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return longSdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Timestamp getLongTimestamp(String time) {
        try {
            SimpleDateFormat longSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return new Timestamp(longSdf.parse(time).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Timestamp getTimestamp(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return new Timestamp(sdf.parse(time).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 日期转日期对象
    public static Date getFullDate(String time) {
        try {
            SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return fullSdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Timestamp getFullTimestamp(String time) {
        try {
            SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return new Timestamp(fullSdf.parse(time).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 计算日期相差天数 date2 - date1
    public static long differentDays(Date date1, Date date2) {
        return cn.hutool.core.date.DateUtil.betweenDay(date1, date2, false);
    }

    /**
     * 日期年份加减
     *
     * @param date 日期
     * @param year 减为负数，加为正数
     * @return
     */
    public static Date asYear(Date date, int year) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.YEAR, year);
            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期月份加减
     *
     * @param date  日期
     * @param month 减为负数，加为正数
     * @return
     */
    public static Date asMonth(Date date, int month) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, month);
            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期分钟加减
     *
     * @param date   日期
     * @param minute 减为负数，加为正数
     * @return
     */
    public static Date asMinute(Date date, int minute) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, minute);
            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期秒数加减
     *
     * @param date
     * @param second
     * @return
     */
    public static Timestamp asSecond(Timestamp date, int second) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.SECOND, second);
            return new Timestamp(calendar.getTime().getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期天数加减
     *
     * @param date 日期
     * @param day  减为负数，加为正数
     * @return
     */
    public static Date asDay(Date date, int day) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, day);
            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期天数加减
     *
     * @param timestamp 日期
     * @param day       减为负数，加为正数
     * @return
     */
    public static Date asDay(Timestamp timestamp, int day) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(timestamp.getTime()));
            calendar.add(Calendar.DATE, day);
            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 判断是否查询下一月数据。15号以前查询当月，返回false
     *
     * @return
     */
    private static boolean isGetNextMonth() {
        Calendar c = Calendar.getInstance();
        int datenum = c.get(Calendar.DATE);
        return datenum >= 15;
    }

    /**
     * 获取当月
     *
     * @return
     */
    private static Date[] getCurrentDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        return new Date[]{c.getTime(), ca.getTime()};
    }

    /**
     * 获取下月
     *
     * @return
     */
    private static Date[] getNextDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.MONTH, 1);
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        return new Date[]{c.getTime(), ca.getTime()};
    }

    /**
     * 获取默认的查询日期
     *
     * @return
     */
    public static Date[] getDefaultQueryDate() {
        if (isGetNextMonth()) {
            return getNextDate();
        } else {
            return getCurrentDate();
        }
    }

    public static String getHhSs(Timestamp time) {
        if (time == null) {
            return null;
        }
        SimpleDateFormat fromat = new SimpleDateFormat("HH:mm");
        return fromat.format(time);
    }

    /**
     * 两日期相减获取分钟数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int getMin(Date date1, Date date2) {
        long between = (date1.getTime() - date2.getTime()) / 1000;//除以1000是为了转换成秒
        long min = between / 60;
        return Integer.valueOf(String.valueOf(min));
    }

    /**
     * 两日期相减获取分钟数
     *
     * @param date1
     * @param date2
     * @return 00分00秒
     */
    public static String getMMSS(Date date1, Date date2) {
        long between = (date1.getTime() - date2.getTime()) / 1000;//除以1000是为了转换成秒
        return String.format("%s分%s秒", between / 60, between % 60);
    }

    /***
     *  计算两个时间相差分钟和秒数
     */
    public static JSONObject dateDiff(String startTime, String endTime) {
        return dateDiff(getFullTimestamp(startTime), getFullTimestamp(endTime));
    }

    public static JSONObject dateDiff(Timestamp startTime, Timestamp endTime) {
        JSONObject object = new JSONObject();
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数
        long ns = 1000;// 一秒钟的毫秒数
        long diff;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        // 获得两个时间的毫秒时间差异
        diff = endTime.getTime() - startTime.getTime();
        hour = (diff / (60 * 60 * 1000));
        min = ((diff / (60 * 1000)) - hour * 60);
        sec = (diff / 1000 - hour * 60 * 60 - min * 60);
        object.put("hour", hour);
        object.put("min", min);
        object.put("sec", sec);
        return object;
    }

    public static long diffSec(Timestamp start, Timestamp end) {
        long diff = end.getTime() - start.getTime();
        return diff / 1000;// 计算差多少秒
    }

    public static long diffSec(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        return diff / 1000;// 计算差多少秒
    }

    public static String formatDiff(JSONObject obj) {
        String min = Objects.toString(obj.get("min"));
        if (min.length() == 1) {
            min = "0" + min;
        }
        String sec = Objects.toString(obj.get("sec"));
        if (sec.length() == 1) {
            sec = "0" + sec;
        }
        return min + ":" + sec;
    }

    public static String asSec(String time, int sec) {
        try {
            String[] timeArr = time.split(":");
            int minute = Integer.valueOf(timeArr[0]);
            int second = Integer.valueOf(timeArr[1]);
            second += sec;
            minute += (second / 60);
            second = second % 60;
            String mStr = String.valueOf(minute);
            String sStr = String.valueOf(second);
            if (minute < 10)
                mStr = "0" + mStr;
            if (second < 10)
                sStr = "0" + sStr;
            return mStr + ":" + sStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    /**
     * @return 当前系统的Unix时间戳
     */
    public static Long getCurrentTimeUnix() {
        //毫秒时间转成分钟
        double doubleTime = (Math.floor(System.currentTimeMillis() / 60000L));
        //往下取整 1.9=> 1.0
        long floorValue = new Double(doubleTime).longValue();
        return floorValue * 60;
    }

    /**
     * 计算两个时间相差多少分钟和秒并转换成double
     *
     * @param date1
     * @param date2
     * @return
     */
    public static double getMinSec(Date date1, Date date2) {
        long min = 0;
        long sec = 0;
        // 获得两个时间的毫秒时间差异
        long diff = date1.getTime() - date2.getTime();
        min = ((diff / (60 * 1000)));
        sec = (diff / 1000 - min * 60);
        Double data = (double) min + sec * 0.01;
        return data;
    }

    public static void parseDateField(Object domain, String dateTimeStr, Field field) {
        Class fieldType = field.getType();
        if (fieldType.equals(Date.class)) {
            Date vdate = null;
            if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                vdate = DateUtil.getFullDate(dateTimeStr);
            } else if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                vdate = DateUtil.getDate(dateTimeStr);
            }
            ReflectUtil.setFieldValue(domain, field, vdate);
        } else if (fieldType.equals(Timestamp.class)) {
            Timestamp vdate = null;
            if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                vdate = DateUtil.getFullTimestamp(dateTimeStr);
            } else if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                vdate = DateUtil.getTimestamp(dateTimeStr);
            }
            ReflectUtil.setFieldValue(domain, field, vdate);
        }
    }
}
