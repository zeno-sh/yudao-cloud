package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 利润计算请求 VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 利润计算请求 VO")
@Data
public class ProfitCalculationReqVO {

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @Schema(description = "产品SKU", example = "ABC-123")
    private String sku;

    @Schema(description = "计划名称", example = "Q1利润测算")
    private String planName;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "Amazon")
    @NotBlank(message = "平台不能为空")
    private String platform;

    @Schema(description = "国家代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "US")
    @NotBlank(message = "国家代码不能为空")
    private String country;

    @Schema(description = "配置模板ID", example = "1")
    private Long templateId;

    // ========== 产品基础信息 ==========

    @Schema(description = "产品长度(cm)", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.5")
    @NotNull(message = "产品长度不能为空")
    @DecimalMin(value = "0.1", message = "产品长度必须大于0.1cm")
    private BigDecimal productLength;

    @Schema(description = "产品宽度(cm)", requiredMode = Schema.RequiredMode.REQUIRED, example = "8.0")
    @NotNull(message = "产品宽度不能为空")
    @DecimalMin(value = "0.1", message = "产品宽度必须大于0.1cm")
    private BigDecimal productWidth;

    @Schema(description = "产品高度(cm)", requiredMode = Schema.RequiredMode.REQUIRED, example = "5.0")
    @NotNull(message = "产品高度不能为空")
    @DecimalMin(value = "0.1", message = "产品高度必须大于0.1cm")
    private BigDecimal productHeight;

    @Schema(description = "产品重量(kg)", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.5")
    @NotNull(message = "产品重量不能为空")
    @DecimalMin(value = "0.001", message = "产品重量必须大于0.001kg")
    private BigDecimal productWeight;

    // ========== 售价和成本信息 ==========

    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED, example = "29.99")
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0.01")
    private BigDecimal price;

    @Schema(description = "币种", example = "USD")
    private String currency;

    @Schema(description = "汇率", example = "6.8")
    @DecimalMin(value = "0.01", message = "汇率必须大于0.01")
    private BigDecimal exchangeRate;

    @Schema(description = "采购价", requiredMode = Schema.RequiredMode.REQUIRED, example = "15.00")
    @NotNull(message = "采购价不能为空")
    @DecimalMin(value = "0.01", message = "采购价必须大于0.01")
    private BigDecimal purchaseCost;

    @Schema(description = "配送费", example = "2.50")
    @DecimalMin(value = "0", message = "配送费不能为负数")
    private BigDecimal deliveryCost;

    @Schema(description = "仓储费", example = "1.20")
    @DecimalMin(value = "0", message = "仓储费不能为负数")
    private BigDecimal storageCost;

    @Schema(description = "海外仓费用", example = "2.00")
    @DecimalMin(value = "0", message = "海外仓费用不能为负数")
    private BigDecimal fbsCost;

    // ========== 自定义配置（可选，覆盖模板配置） ==========

    @Schema(description = "自定义体积系数", example = "200")
    @DecimalMin(value = "1", message = "体积系数必须大于等于1")
    private BigDecimal customVolumeCoefficient;

    @Schema(description = "自定义重量系数", example = "5000")
    @DecimalMin(value = "1", message = "重量系数必须大于等于1")
    private BigDecimal customWeightCoefficient;

    @Schema(description = "自定义关税率(%)", example = "10.5")
    @DecimalMin(value = "0", message = "关税率不能为负数")
    @DecimalMax(value = "100", message = "关税率不能超过100%")
    private BigDecimal customTariffRate;

    @Schema(description = "自定义VAT税率(%)", example = "19.0")
    @DecimalMin(value = "0", message = "VAT税率不能为负数")
    @DecimalMax(value = "100", message = "VAT税率不能超过100%")
    private BigDecimal customVatRate;

    @Schema(description = "自定义佣金率(%)", example = "15.0")
    @DecimalMin(value = "0", message = "佣金率不能为负数")
    @DecimalMax(value = "100", message = "佣金率不能超过100%")
    private BigDecimal customCommissionRate;

}