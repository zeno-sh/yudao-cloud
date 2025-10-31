package cn.iocoder.yudao.module.dm.infrastructure.ozon.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 莫斯科时区(UTC+3)相关的日期工具类
 * 用于处理数据库存储的UTC时间与业务需要的莫斯科时区时间的转换
 *
 * @author: Zeno
 * @createTime: 2024/06/30 14:56
 */
public class DmDateUtils {
    // 定义常用时区常量
    public static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    public static final ZoneId MOSCOW_ZONE_ID = ZoneId.of("Europe/Moscow");
    public static final ZoneId CHINA_ZONE_ID = ZoneId.of("Asia/Shanghai");
    
    // 定义常用日期格式
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前莫斯科时区的日期
     */
    public static String getMoscowToday() {
        return ZonedDateTime.now(MOSCOW_ZONE_ID).format(DATE_FORMATTER);
    }

    /**
     * 获取莫斯科时区的昨天日期
     */
    public static String getMoscowYesterday() {
        return ZonedDateTime.now(MOSCOW_ZONE_ID).minusDays(1).format(DATE_FORMATTER);
    }

    /**
     * 获取莫斯科时区的明天日期
     */
    public static String getMoscowTomorrow() {
        return ZonedDateTime.now(MOSCOW_ZONE_ID).plusDays(1).format(DATE_FORMATTER);
    }

    /**
     * 获取莫斯科时区指定日期的开始时间（0点）对应的UTC时间
     */
    public static String formatStartOfDay(String moscowDateString, String formatterPattern) {
        if (StringUtils.isBlank(moscowDateString)) {
            // 如果没有传入日期字符串，使用当前莫斯科日期
            return getCurrentMoscowDateTime().truncatedTo(ChronoUnit.DAYS)
                    .atZone(MOSCOW_ZONE_ID)
                    .withZoneSameInstant(UTC_ZONE_ID)
                    .format(DateTimeFormatter.ofPattern(formatterPattern));
        }
        
        // 解析莫斯科日期，获取当天开始时间（0点）
        LocalDate localDate = LocalDate.parse(moscowDateString, DATE_FORMATTER);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        
        // 转换为UTC时间并格式化
        return startOfDay.atZone(MOSCOW_ZONE_ID)
                .withZoneSameInstant(UTC_ZONE_ID)
                .format(DateTimeFormatter.ofPattern(formatterPattern));
    }

    /**
     * 获取莫斯科时区指定日期的结束时间（23:59:59.999999999）对应的UTC时间
     */
    public static String formatEndOfDay(String moscowDateString, String formatterPattern) {
        ZonedDateTime moscowDateTime;
        
        if (StringUtils.isBlank(moscowDateString)) {
            // 如果没有传入日期字符串，使用当前莫斯科日期
            moscowDateTime = ZonedDateTime.now(MOSCOW_ZONE_ID).with(LocalTime.MAX);
        } else {
            // 解析莫斯科日期，获取当天结束时间（23:59:59.999999999）
            LocalDate localDate = LocalDate.parse(moscowDateString, DATE_FORMATTER);
            moscowDateTime = localDate.atTime(LocalTime.MAX).atZone(MOSCOW_ZONE_ID);
        }
        
        // 转换为UTC时间并格式化
        return moscowDateTime.withZoneSameInstant(UTC_ZONE_ID)
                .format(DateTimeFormatter.ofPattern(formatterPattern));
    }

    /**
     * 将UTC时间转换为莫斯科时区的日期
     */
    public static LocalDate convertUtcToMoscowLocalDate(LocalDateTime utcDateTime) {
        return utcDateTime.atZone(UTC_ZONE_ID)
                .withZoneSameInstant(MOSCOW_ZONE_ID)
                .toLocalDate();
    }

    /**
     * 将UTC时间转换为莫斯科时区的日期时间
     */
    public static LocalDateTime convertUtcToMoscowLocalDateTime(LocalDateTime utcDateTime) {
        return utcDateTime.atZone(UTC_ZONE_ID)
                .withZoneSameInstant(MOSCOW_ZONE_ID)
                .toLocalDateTime();
    }

    /**
     * 获取莫斯科时区指定日期的开始时间戳（秒）
     */
    public static long formatStartOfDayTimestamp(String moscowDateString) {
        ZonedDateTime moscowDateTime;
        
        if (StringUtils.isBlank(moscowDateString)) {
            moscowDateTime = ZonedDateTime.now(MOSCOW_ZONE_ID).truncatedTo(ChronoUnit.DAYS);
        } else {
            LocalDate localDate = LocalDate.parse(moscowDateString, DATE_FORMATTER);
            moscowDateTime = localDate.atStartOfDay().atZone(MOSCOW_ZONE_ID);
        }
        
        // 转换为UTC时间并获取时间戳
        return moscowDateTime.withZoneSameInstant(UTC_ZONE_ID)
                .toInstant().getEpochSecond();
    }

    /**
     * 获取莫斯科时区指定日期的结束时间戳（秒）
     */
    public static long formatEndOfDayTimestamp(String moscowDateString) {
        ZonedDateTime moscowDateTime;
        
        if (StringUtils.isBlank(moscowDateString)) {
            moscowDateTime = ZonedDateTime.now(MOSCOW_ZONE_ID).with(LocalTime.MAX);
        } else {
            LocalDate localDate = LocalDate.parse(moscowDateString, DATE_FORMATTER);
            moscowDateTime = localDate.atTime(LocalTime.MAX).atZone(MOSCOW_ZONE_ID);
        }
        
        // 转换为UTC时间并获取时间戳
        return moscowDateTime.withZoneSameInstant(UTC_ZONE_ID)
                .toInstant().getEpochSecond();
    }

    /**
     * 解析莫斯科时区的日期范围，转换为UTC时间范围
     * 兼容性考虑，保留旧方法
     */
    public static LocalDateTime[] parseMoscowLocalDate(String beginDate, String endDate) {
        return convertMoscowDateRangeToUtcTime(beginDate, endDate);
    }

    /**
     * 转换莫斯科日期范围为UTC时间范围
     */
    public static LocalDateTime[] convertMoscowDateRangeToUtcTime(String moscowDateStart, String moscowDateEnd) {
        // 处理默认值
        if (moscowDateStart == null || moscowDateEnd == null) {
            moscowDateStart = getMoscowYesterday();
            moscowDateEnd = getMoscowToday();
        }
        
        // 获取开始和结束时间的UTC表示
        String beginDateTime = formatStartOfDay(moscowDateStart, DatePattern.NORM_DATETIME_PATTERN);
        String endDateTime = formatEndOfDay(moscowDateEnd, DatePattern.NORM_DATETIME_PATTERN);
        
        // 转换为LocalDateTime对象
        LocalDateTime[] result = new LocalDateTime[2];
        result[0] = LocalDateTimeUtil.of(DateUtil.parseDateTime(beginDateTime).toInstant());
        result[1] = LocalDateTimeUtil.of(DateUtil.parseDateTime(endDateTime).toInstant());
        
        return result;
    }
    
    /**
     * 处理VO对象中的日期数组，转换为UTC时间范围的LocalDateTime数组
     * 
     * @param dateArray 日期数组，预期格式为 [开始日期, 结束日期]
     * @return UTC时间范围数组，如果输入为空则返回null
     */
    public static LocalDateTime[] convertVODateArrayToUtcTimeRange(String[] dateArray) {
        if (null == dateArray || dateArray.length == 0) {
            return null;
        }
        
        String beginDate = dateArray[0];
        String endDate = dateArray.length > 1 ? dateArray[1] : beginDate;
        
        return convertMoscowDateRangeToUtcTime(beginDate, endDate);
    }
    
    /**
     * 判断一个UTC时间是否在莫斯科日期范围内
     */
    public static boolean isUtcTimeInMoscowDateRange(LocalDateTime utcTime, LocalDate moscowStartDate, LocalDate moscowEndDate) {
        // 将UTC时间转换为莫斯科时区的日期
        LocalDate moscowDate = convertUtcToMoscowLocalDate(utcTime);
        
        // 判断是否在莫斯科日期范围内
        return !moscowDate.isBefore(moscowStartDate) && !moscowDate.isAfter(moscowEndDate);
    }
    
    /**
     * 将UTC时间范围转换为莫斯科时区的展示格式
     */
    public static String formatUtcTimeRangeToMoscow(LocalDateTime utcTimeStart, LocalDateTime utcTimeEnd) {
        LocalDateTime moscowTimeStart = convertUtcToMoscowLocalDateTime(utcTimeStart);
        LocalDateTime moscowTimeEnd = convertUtcToMoscowLocalDateTime(utcTimeEnd);
        
        return DATETIME_FORMATTER.format(moscowTimeStart) + " ~ " + DATETIME_FORMATTER.format(moscowTimeEnd) + " (莫斯科时间)";
    }
    
    /**
     * 获取当前莫斯科时区的日期时间
     */
    public static LocalDateTime getCurrentMoscowDateTime() {
        return ZonedDateTime.now(MOSCOW_ZONE_ID).toLocalDateTime();
    }
    
    /**
     * 获取当前莫斯科时区的日期
     */
    public static LocalDate getCurrentMoscowDate() {
        return ZonedDateTime.now(MOSCOW_ZONE_ID).toLocalDate();
    }
    
    /**
     * 将莫斯科时区时间转换为UTC时间
     */
    public static LocalDateTime convertMoscowToUtcDateTime(LocalDateTime moscowDateTime) {
        return moscowDateTime.atZone(MOSCOW_ZONE_ID)
                .withZoneSameInstant(UTC_ZONE_ID)
                .toLocalDateTime();
    }
    
    /**
     * 生成莫斯科时区的日期区间列表，用于按天/周/月/季/年统计
     * 
     * @param dateType 日期类型 1-日 2-周 3-月 4-季 5-年
     * @param startDate 莫斯科时区开始日期
     * @param endDate 莫斯科时区结束日期
     * @return 日期区间列表，按降序排列
     */
    public static List<String> generateMoscowDateRangeList(Integer dateType, LocalDate startDate, LocalDate endDate) {
        List<String> dateList = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        switch (dateType) {
            case 1: // 按日
                while (!currentDate.isAfter(endDate)) {
                    dateList.add(currentDate.toString());
                    currentDate = currentDate.plusDays(1);
                }
                break;
                
            case 2: // 按周
                while (!currentDate.isAfter(endDate)) {
                    LocalDate weekEnd = currentDate.plusDays(6);
                    if (weekEnd.isAfter(endDate)) weekEnd = endDate;
                    dateList.add(currentDate.toString() + "~" + weekEnd.toString());
                    currentDate = weekEnd.plusDays(1);
                }
                break;
                
            case 3: // 按月
                while (!currentDate.isAfter(endDate)) {
                    LocalDate monthEnd = currentDate.with(TemporalAdjusters.lastDayOfMonth());
                    if (monthEnd.isAfter(endDate)) monthEnd = endDate;
                    dateList.add(currentDate.toString() + "~" + monthEnd.toString());
                    currentDate = monthEnd.plusDays(1);
                }
                break;
                
            case 4: // 按季
                while (!currentDate.isAfter(endDate)) {
                    LocalDate quarterEnd = currentDate.plusMonths(3).with(TemporalAdjusters.lastDayOfMonth());
                    if (quarterEnd.isAfter(endDate)) quarterEnd = endDate;
                    dateList.add(currentDate.toString() + "~" + quarterEnd.toString());
                    currentDate = quarterEnd.plusDays(1);
                }
                break;
                
            case 5: // 按年
                while (!currentDate.isAfter(endDate)) {
                    LocalDate yearEnd = currentDate.with(TemporalAdjusters.lastDayOfYear());
                    if (yearEnd.isAfter(endDate)) yearEnd = endDate;
                    dateList.add(currentDate.toString() + "~" + yearEnd.toString());
                    currentDate = yearEnd.plusDays(1);
                }
                break;
                
            default:
                throw new IllegalArgumentException("不支持的日期类型: " + dateType);
        }
        
        // 倒序排列
        Collections.reverse(dateList);
        return dateList;
    }
}
