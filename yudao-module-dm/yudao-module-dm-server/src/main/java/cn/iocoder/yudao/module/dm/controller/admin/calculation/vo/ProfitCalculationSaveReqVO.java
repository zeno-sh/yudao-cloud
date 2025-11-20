package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 利润预测新增/修改 Request VO")
@Data
public class ProfitCalculationSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "16777")
    private Long id;

    @Schema(description = "国家", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "国家不能为空")
    private String country;

    @Schema(description = "本地产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31633")
    private Long productId;

    @Schema(description = "选品计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "选品计划名称不能为空")
    private String planName;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer platform;

    @Schema(description = "配置模板ID", example = "1")
    private Long templateId;

    // 售价和汇率
    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED, example = "25053")
    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    @Schema(description = "汇率", example = "6.8")
    private BigDecimal exchangeRate;

    // 基础产品信息
    @Schema(description = "产品长度(cm)", example = "10.5")
    private BigDecimal productLength;

    @Schema(description = "产品宽度(cm)", example = "8.0")
    private BigDecimal productWidth;

    @Schema(description = "产品高度(cm)", example = "5.2")
    private BigDecimal productHeight;

    @Schema(description = "产品重量(kg)", example = "0.8")
    private BigDecimal productWeight;

    // 成本、费率
    @Schema(description = "采购单价（CNY，不含税）", example = "50.00")
    private BigDecimal purchaseUnit;
    
    @Schema(description = "采购成本（目标币种，根据汇率计算）", example = "50.00")
    private BigDecimal purchaseCost;

    @Schema(description = "配送费", example = "10.00")
    private BigDecimal deliveryCost;

    @Schema(description = "配送费", example = "10.00")
    private BigDecimal storageCost;

    @Schema(description = "海运计费方式：1-按体积，2-按重量", example = "2")
    private Integer shippingCalculationType;

    @Schema(description = "海运单价", example = "15220")
    private BigDecimal shippingUnitPrice;

    @Schema(description = "国内运费单价（每立方米）")
    private BigDecimal domesticFreightUnit;

    @Schema(description = "货代费用单价（每立方米）")
    private BigDecimal freightForwarderUnit;

    @Schema(description = "关税率(%)")
    private BigDecimal tariffRate;

    @Schema(description = "VAT税率(%)")
    private BigDecimal vatRate;

    @Schema(description = "申报比例(%)")
    private BigDecimal declarationRatio;

    @Schema(description = "类目佣金率(%)")
    private BigDecimal categoryCommissionRate;

    @Schema(description = "数字服务费率(%)（英国等）")
    private BigDecimal digitalServiceRate;

    @Schema(description = "是否启用FBA：1-是，0-否")
    private Integer fbaEnabled;

    @Schema(description = "广告费率(%)")
    private BigDecimal adRate;

    @Schema(description = "退货率(%)")
    private BigDecimal returnRate;

    // 其他配置
    @Schema(description = "体积系数（立方米转重量）")
    private BigDecimal volumeCoefficient;

    @Schema(description = "重量系数（重量转体积）")
    private BigDecimal weightCoefficient;

    @Schema(description = "默认币种代码")
    private String currencyCode;

}