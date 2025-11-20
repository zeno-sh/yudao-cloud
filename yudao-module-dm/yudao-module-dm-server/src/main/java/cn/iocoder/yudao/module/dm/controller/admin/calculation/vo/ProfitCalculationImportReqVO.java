package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 利润预测 Excel 导入 Request VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitCalculationImportReqVO {

    @Schema(description = "本地产品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本地产品ID")
    @NotNull(message = "本地产品ID不能为空")
    private Long productId;

    @Schema(description = "选品计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("选品计划名称")
    @NotNull(message = "选品计划名称不能为空")
    private String planName;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("平台")
    @ExcelColumnSelect(dictType = "dm_platform")
    @NotNull(message = "平台不能为空")
    private Integer platform;

    @Schema(description = "国家", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("国家")
    @ExcelColumnSelect(functionName = "demoCountrySelectFunction")
    @NotNull(message = "国家不能为空")
    private String country;

    @Schema(description = "配置模板ID")
    @ExcelProperty("配置模板ID")
    private Long templateId;

    // 基础产品信息
    @Schema(description = "产品SKU")
    @ExcelProperty("产品SKU")
    private String sku;

    @Schema(description = "产品长度(cm)")
    @ExcelProperty("产品长度(cm)")
    @DecimalMin(value = "0", message = "产品长度不能小于0")
    private BigDecimal productLength;

    @Schema(description = "产品宽度(cm)")
    @ExcelProperty("产品宽度(cm)")
    @DecimalMin(value = "0", message = "产品宽度不能小于0")
    private BigDecimal productWidth;

    @Schema(description = "产品高度(cm)")
    @ExcelProperty("产品高度(cm)")
    @DecimalMin(value = "0", message = "产品高度不能小于0")
    private BigDecimal productHeight;

    @Schema(description = "产品重量(kg)")
    @ExcelProperty("产品重量(kg)")
    @DecimalMin(value = "0", message = "产品重量不能小于0")
    private BigDecimal productWeight;

    // 售价和汇率
    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("售价")
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0", message = "售价不能小于0")
    private BigDecimal price;

    @Schema(description = "币种")
    @ExcelProperty("币种")
    @ExcelColumnSelect(dictType = "dm_currency_code")
    private String currencyCode;

    @Schema(description = "汇率")
    @ExcelProperty("汇率")
    @DecimalMin(value = "0", message = "汇率不能小于0")
    private BigDecimal exchangeRate;

    // 成本输入
    @Schema(description = "采购价（CNY，不含税）")
    @ExcelProperty("采购价（CNY，不含税）")
    @DecimalMin(value = "0", message = "采购价不能小于0")
    private BigDecimal purchaseCost;

    @Schema(description = "配送费")
    @ExcelProperty("配送费")
    @DecimalMin(value = "0", message = "配送费不能小于0")
    private BigDecimal deliveryCost;

    @Schema(description = "仓储费")
    @ExcelProperty("仓储费")
    @DecimalMin(value = "0", message = "仓储费不能小于0")
    private BigDecimal storageCost;

}