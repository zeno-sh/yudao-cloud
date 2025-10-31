package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 发货计划报表导出 Response VO")
@Data
public class TransportReportExportVO {

    @Schema(description = "月份，格式为：yyyy-MM", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-12")
    @ExcelProperty("月份")
    private String reportDate;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("产品编号")
    private Long productId;

    @Schema(description = "SKU", requiredMode = Schema.RequiredMode.REQUIRED, example = "SKU001")
    @ExcelProperty("SKU")
    private String skuId;

    @Schema(description = "SKU名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试商品")
    @ExcelProperty("SKU名称")
    private String skuName;

    @Schema(description = "期初在途数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("期初在途")
    private Integer beginTransitQuantity;

    @Schema(description = "本期在途数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @ExcelProperty("本期在途")
    private Integer currentTransitQuantity;

    @Schema(description = "本期到仓数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @ExcelProperty("本期到仓")
    private Integer currentArrivalQuantity;

    @Schema(description = "期末在途数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
    @ExcelProperty("期末在途")
    private Integer endTransitQuantity;
    
    @Schema(description = "在途货值(不含税)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
    @ExcelProperty("在途货值(不含税)")
    private BigDecimal totalPrice;

    @Schema(description = "在途货值(含税)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1100.00")
    @ExcelProperty("在途货值(含税)")
    private BigDecimal totalTaxPrice;
}