package cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 采购单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PurchaseOrderPageReqVO extends PageParam {

    @Schema(description = "采购单号")
    private String orderNo;

    @Schema(description = "单据负责人")
    private Integer owner;

    @Schema(description = "结算方式", example = "2")
    private Integer settleType;

    @Schema(description = "是否含税 ")
    private Boolean tax;

    @Schema(description = "合计金额")
    private BigDecimal totalAmount;

    @Schema(description = "采购状态", example = "1")
    private Integer status;

    @Schema(description = "付款状态", example = "1")
    private Integer paymentStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "是否可付款", example = "true")
    private Boolean paymentEnable;

    @Schema(description = "计划编号")
    private String planNumber;

    @Schema(description = "批次编号")
    private String batchNumber;

    @Schema(description = "供应商")
    private Long supplierId;

    @Schema(description = "本地产品ID")
    private Long productId;

}