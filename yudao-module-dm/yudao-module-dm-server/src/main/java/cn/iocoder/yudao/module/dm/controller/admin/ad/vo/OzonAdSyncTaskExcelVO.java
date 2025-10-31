package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ozon广告同步任务 Excel VO
 *
 * @author Jax
 */
@Data
public class OzonAdSyncTaskExcelVO {

    @ExcelProperty("任务ID")
    private Long id;

    @ExcelProperty("租户ID")
    private Long tenantId;

    @ExcelProperty("客户端ID")
    private String clientId;

    @ExcelProperty("开始日期")
    private LocalDate beginDate;

    @ExcelProperty("结束日期")
    private LocalDate endDate;

    @ExcelProperty("任务状态")
    private String statusDesc;

    @ExcelProperty("报告UUID")
    private String reportUuid;

    @ExcelProperty("重试次数")
    private Integer retryCount;

    @ExcelProperty("最大重试次数")
    private Integer maxRetryCount;

    @ExcelProperty("下次重试时间")
    private LocalDateTime nextRetryTime;

    @ExcelProperty("错误信息")
    private String errorMessage;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @ExcelProperty("完成时间")
    private LocalDateTime finishTime;

    @ExcelProperty("已处理数量")
    private Integer processedCount;

    @ExcelProperty("任务参数")
    private String taskParams;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("执行时长(秒)")
    private Long duration;

}