package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 采购计划详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PurchasePlanItemRespVO {

    @Schema(description = "计划编号")
    @ExcelProperty("计划编号")
    private String planNumber;

    @Schema(description = "计划id", requiredMode = Schema.RequiredMode.REQUIRED, example = "27495")
    @ExcelProperty("计划id")
    private Long planId;

    @Schema(description = "计划批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27495")
    @ExcelProperty("计划批次编号")
    private String batchNumber;

    @Schema(description = "产品Id", example = "31704")
    @ExcelProperty("产品Id")
    private Long productId;

    @Schema(description = "采购数量")
    @ExcelProperty("采购数量")
    private Integer quantity;

    @Schema(description = "pcs")
    @ExcelProperty("pcs")
    private Integer pcs;

    @Schema(description = "箱数")
    @ExcelProperty("箱数")
    private Integer numberOfBox;

    @Schema(description = "体积")
    @ExcelProperty("体积")
    private BigDecimal volume;

    @Schema(description = "重量")
    @ExcelProperty("重量")
    private BigDecimal weight;

    @Schema(description = "采购订单")
    @ExcelProperty("采购订单")
    private String purchaseOrder;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "采购状态", example = "2")
    @ExcelProperty(value = "采购状态", converter = DictConvert.class)
    @DictFormat("dm_purchase_plan_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "审核状态", example = "2")
    @ExcelProperty(value = "审核状态", converter = DictConvert.class)
    @DictFormat("bpm_task_status")
    private Integer auditStatus;

    @Schema(description = "期望到货时间")
    @ExcelProperty("期望到货时间")
    private LocalDateTime expectedArrivalDate;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creatorName;

    @Schema(description = "产品信息")
    private ProductSimpleInfoVO productSimpleInfo;
}