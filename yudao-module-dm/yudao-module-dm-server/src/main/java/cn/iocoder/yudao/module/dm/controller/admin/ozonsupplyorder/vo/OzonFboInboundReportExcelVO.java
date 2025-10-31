package cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema(description = "管理后台 - Ozon FBO进仓报表 Excel VO")
@Data
@Accessors(chain = true)
public class OzonFboInboundReportExcelVO {

    @ExcelProperty("月份")
    @Schema(description = "月份，格式为yyyy-MM")
    private String month;

    @ExcelProperty("Sku")
    @Schema(description = "Sku")
    private String skuId;

    @ExcelProperty("商品名称")
    @Schema(description = "商品名称")
    private String productName;

    @ExcelProperty("期初结余")
    @Schema(description = "期初结余")
    private Integer initialBalance;

    @ExcelProperty("本期进仓数量")
    @Schema(description = "本期进仓数量")
    private Integer inboundQuantity;

    @ExcelProperty("本期销售数量")
    @Schema(description = "本期销售数量")
    private Integer salesQuantity;

    @ExcelProperty("期末结余")
    @Schema(description = "期末结余")
    private Integer finalBalance;

    @ExcelProperty("供应商报价")
    @Schema(description = "供应商报价")
    private BigDecimal supplierPrice;

    @ExcelProperty("进仓货值(不含税)")
    @Schema(description = "进仓货值(不含税)")
    private BigDecimal taxIncludedAmount;

    @ExcelProperty("进仓货值(含税)")
    @Schema(description = "进仓货值(含税)")
    private BigDecimal taxExcludedAmount;
} 