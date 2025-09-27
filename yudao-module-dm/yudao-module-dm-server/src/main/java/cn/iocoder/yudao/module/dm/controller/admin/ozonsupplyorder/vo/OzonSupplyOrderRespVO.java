package cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 供应订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonSupplyOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "24795")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6149")
    @ExcelProperty("门店ID")
    private String clientId;

    @Schema(description = "Ozon供应订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7438")
    @ExcelProperty("Ozon供应订单ID")
    private Long supplyOrderId;

    @Schema(description = "申请号码", requiredMode = Schema.RequiredMode.REQUIRED, example = "7438")
    @ExcelProperty("申请号码")
    private String supplyOrderNumber;

    @Schema(description = "创建日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "7438")
    @ExcelProperty("创建日期")
    private LocalDate creationDate;

    @Schema(description = "订单状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("订单状态")
    private String state;

    @Schema(description = "仓库ID", example = "27899")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "李四")
    @ExcelProperty("仓库名称")
    private String warehouseName;

    @Schema(description = "配送时间段开始")
    @ExcelProperty("配送时间段开始")
    private LocalDateTime timeslotFrom;

    @Schema(description = "配送时间段结束")
    @ExcelProperty("配送时间段结束")
    private LocalDateTime timeslotTo;

    @Schema(description = "商品种类")
    @ExcelProperty("商品种类")
    private Integer skuCount;

    @Schema(description = "商品总数")
    @ExcelProperty("商品总数")
    private Integer totalItems;

    @Schema(description = "总体积(升)")
    @ExcelProperty("总体积(升)")
    private BigDecimal totalVolume;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "手动编辑")
    @ExcelProperty("手动编辑")
    private Boolean updatedManually;

}