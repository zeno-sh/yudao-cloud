package cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 采购单到货日志 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PurchaseOrderArrivedLogRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4220")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "采购单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3160")
    @ExcelProperty("采购单ID")
    private Long purchaseOrderId;

    @Schema(description = "采购单item ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10027")
    @ExcelProperty("采购单item ID")
    private Long purchaseOrderItemId;

    @Schema(description = "到货数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("到货数量")
    private Integer arrivedQuantity;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "采购计划编号")
    @ExcelProperty("采购计划编号")
    private String planNumber;

    @Schema(description = "产品ID")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "创建人ID")
    @ExcelProperty("创建人ID")
    private String creator;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creatorName;

}