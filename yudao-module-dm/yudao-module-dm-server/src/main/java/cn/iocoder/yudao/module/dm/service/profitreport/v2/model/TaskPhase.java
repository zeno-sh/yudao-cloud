package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务执行阶段枚举
 *
 * @author Jax
 */
@Getter
@AllArgsConstructor
public enum TaskPhase {

    INITIALIZED("已初始化", 0),
    DATA_COLLECTION_STARTED("数据收集开始", 10),
    DATA_COLLECTION_COMPLETED("数据收集完成", 30),
    CALCULATION_STARTED("成本计算开始", 40),
    CALCULATION_COMPLETED("成本计算完成", 70),
    AGGREGATION_STARTED("数据聚合开始", 80),
    AGGREGATION_COMPLETED("数据聚合完成", 90),
    RESULT_SAVING("结果保存中", 95),
    COMPLETED("计算完成", 100),
    FAILED("计算失败", -1),
    CANCELLED("已取消", -2);

    /**
     * 阶段名称
     */
    private final String name;

    /**
     * 进度百分比
     */
    private final Integer progress;
} 