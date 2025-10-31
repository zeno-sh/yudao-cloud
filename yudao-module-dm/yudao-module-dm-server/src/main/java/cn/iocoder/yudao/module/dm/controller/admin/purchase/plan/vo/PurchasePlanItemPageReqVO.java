package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 采购计划详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PurchasePlanItemPageReqVO extends PageParam {

    @Schema(description = "计划组编号")
    private String batchNumber;

    @Schema(description = "计划组编号")
    private Long planId;

    @Schema(description = "计划编号")
    private String planNumber;

    @Schema(description = "产品Id", example = "31704")
    private Long productId;

    @Schema(description = "spu", example = "wxxx")
    private String spu;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "采购状态", example = "2")
    private Integer status;

    @Schema(description = "审批状态", example = "2")
    private Integer auditStatus;

    private List<Long> productIds;

}