package cn.iocoder.yudao.module.dm.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.dm.enums.DateTypeEnum;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * DM 模块的日期工具类
 */
public class DmDateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("YYYY-'W'ww");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter QUARTER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-'Q'Q");

    /**
     * 获取时间维度的格式化字符串
     */
    public static String formatDate(LocalDate date, Integer dateType) {
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(dateType);
        switch (dateTypeEnum) {
            case DATE_TYPE_1: // 日
                return date.format(DATE_FORMATTER);
            case DATE_TYPE_2: // 周
                return date.format(WEEK_FORMATTER);
            case DATE_TYPE_3: // 月
                return date.format(MONTH_FORMATTER);
            case DATE_TYPE_4: // 季度
                return date.format(QUARTER_FORMATTER);
            default:
                throw new IllegalArgumentException("Unsupported date type: " + dateType);
        }
    }

    /**
     * 获取时间维度的开始日期
     */
    public static LocalDate getStartDate(LocalDate date, Integer dateType) {
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(dateType);
        switch (dateTypeEnum) {
            case DATE_TYPE_1: // 日
                return date;
            case DATE_TYPE_2: // 周
                return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case DATE_TYPE_3: // 月
                return date.with(TemporalAdjusters.firstDayOfMonth());
            case DATE_TYPE_4: // 季度
                return date.with(date.getMonth().firstMonthOfQuarter())
                          .with(TemporalAdjusters.firstDayOfMonth());
            default:
                throw new IllegalArgumentException("Unsupported date type: " + dateType);
        }
    }

    /**
     * 获取时间维度的结束日期（不包含）
     */
    public static LocalDate getEndDate(LocalDate date, Integer dateType) {
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(dateType);
        switch (dateTypeEnum) {
            case DATE_TYPE_1: // 日
                return date.plusDays(1);
            case DATE_TYPE_2: // 周
                return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(1);
            case DATE_TYPE_3: // 月
                return date.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(1);
            case DATE_TYPE_4: // 季度
                return date.with(date.getMonth().firstMonthOfQuarter())
                          .with(TemporalAdjusters.firstDayOfMonth())
                          .plusMonths(3);
            default:
                throw new IllegalArgumentException("Unsupported date type: " + dateType);
        }
    }

    /**
     * 生成时间维度的日期列表
     */
    public static List<String> generateDateList(Integer dateType, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }

        List<String> dateList = new ArrayList<>();
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(dateType);
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            LocalDate periodStart = getStartDate(current, dateType);
            dateList.add(formatDate(periodStart, dateType));
            
            // 移动到下一个周期
            switch (dateTypeEnum) {
                case DATE_TYPE_1: // 日
                    current = current.plusDays(1);
                    break;
                case DATE_TYPE_2: // 周
                    current = current.plusWeeks(1);
                    break;
                case DATE_TYPE_3: // 月
                    current = current.plusMonths(1);
                    break;
                case DATE_TYPE_4: // 季度
                    current = current.plusMonths(3);
                    break;
            }
        }

        Collections.reverse(dateList);
        return dateList;
    }

    /**
     * 获取时间维度的分组键
     */
    public static String getTimeGroupKey(LocalDate date, Integer dateType) {
        return formatDate(getStartDate(date, dateType), dateType);
    }

    /**
     * 获取上一个时间段的日期范围
     */
    public static LocalDateTime[] getPreviousPeriodRange(LocalDateTime currentStartDate, Integer dateType) {
        LocalDate date = currentStartDate.toLocalDate();
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(dateType);
        
        // 1. 先调整到当前周期的开始
        LocalDate periodStart = getStartDate(date, dateType);
        
        // 2. 然后获取上一个周期的开始和结束
        LocalDate previousStart;
        LocalDate previousEnd;
        switch (dateTypeEnum) {
            case DATE_TYPE_1: // 日
                previousStart = periodStart.minusDays(1);
                previousEnd = periodStart;
                break;
            case DATE_TYPE_2: // 周
                previousStart = periodStart.minusWeeks(1);
                previousEnd = periodStart;
                break;
            case DATE_TYPE_3: // 月
                previousStart = periodStart.minusMonths(1);
                previousEnd = periodStart;
                break;
            case DATE_TYPE_4: // 季度
                previousStart = periodStart.minusMonths(3);
                previousEnd = periodStart;
                break;
            default:
                throw new IllegalArgumentException("Unsupported date type: " + dateType);
        }

        return new LocalDateTime[]{
            previousStart.atStartOfDay(),
            previousEnd.atStartOfDay().minusNanos(1)
        };
    }

    /**
     * 格式化日期范围为字符串数组
     */
    public static String[] formatDateRange(LocalDateTime[] dateTimeRange) {
        return new String[]{
            dateTimeRange[0].toLocalDate().format(DATE_FORMATTER),
            dateTimeRange[1].toLocalDate().format(DATE_FORMATTER)
        };
    }
} 