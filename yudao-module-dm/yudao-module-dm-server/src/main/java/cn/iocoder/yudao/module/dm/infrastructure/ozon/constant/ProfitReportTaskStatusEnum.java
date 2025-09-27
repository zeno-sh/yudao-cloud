package cn.iocoder.yudao.module.dm.infrastructure.ozon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 财务账单报告任务状态枚举
 *
 * @author zeno
 */
@Getter
@AllArgsConstructor
public enum ProfitReportTaskStatusEnum {

    RUNNING(0, "执行中"),
    SUCCESS(1, "执行成功"),
    FAILED(2, "执行失败"),
    ;


    /**
     * 状态
     */
    private final Integer status;
    /**
     * 描述
     */
    private final String desc;

}