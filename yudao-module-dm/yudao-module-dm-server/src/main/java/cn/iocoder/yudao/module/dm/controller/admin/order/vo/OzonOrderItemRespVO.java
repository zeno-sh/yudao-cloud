package cn.iocoder.yudao.module.dm.controller.admin.order.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - Ozon订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonOrderItemRespVO {

    @Schema(description = "平台门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "22208")
    @ExcelProperty("平台门店id")
    private String clientId;

    @Schema(description = "平台订单id", requiredMode = Schema.RequiredMode.REQUIRED, example = "287")
    @ExcelProperty("平台订单id")
    private String orderId;

    @Schema(description = "发货编号")
    @ExcelProperty("发货编号")
    private String postingNumber;

    @Schema(description = "货号", example = "1")
    @ExcelProperty("货号")
    private String offerId;

    @Schema(description = "货号", example = "1")
    @ExcelProperty("货号")
    private Integer quantity;

    @Schema(description = "货号", example = "1")
    @ExcelProperty("货号")
    private BigDecimal price;

    @Schema(description = "产品图片", example = "1")
    @ExcelProperty("产品图片")
    private String image;
}