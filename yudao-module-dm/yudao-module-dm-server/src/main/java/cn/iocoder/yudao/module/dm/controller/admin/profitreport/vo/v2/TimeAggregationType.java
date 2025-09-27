package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 时间聚合类型枚举
 *
 * @author Jax
 */
@Getter
@AllArgsConstructor
public enum TimeAggregationType {

    DAY("按日聚合", "每天一条记录"),
    WEEK("按周聚合", "每周一条记录"),
    MONTH("按月聚合", "每月一条记录"),
    TOTAL("全部聚合", "整个时间段一条记录");

    /**
     * 聚合类型名称
     */
    private final String name;

    /**
     * 聚合类型描述
     */
    private final String description;
} 