package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "利润计算结果 VO")
@Data
public class ProfitCalculationResultVO {
    
    // ========== 基础信息 ==========
    /**
     * 产品ID
     */
    @Schema(description = "产品ID", example = "1024")
    private Long productId;

    /**
     * 选品计划名称
     */
    @Schema(description = "计划名称", example = "Q1利润测算")
    private String planName;

    /**
     * 平台
     */
    @Schema(description = "平台", example = "1")
    private Integer platform;

    /**
     * 国家
     */
    @Schema(description = "国家代码", example = "US")
    private String country;

    /**
     * 配置模板ID
     */
    @Schema(description = "配置模板ID", example = "1")
    private Long templateId;
    
    // 计算配置参数
    /**
     * 体积系数（立方米转重量）
     */
    @Schema(description = "体积系数（立方米转重量）", example = "167.000")
    private BigDecimal volumeCoefficient;
    /**
     * 重量系数（重量转体积）
     */
    @Schema(description = "重量系数（重量转体积）", example = "400.000")
    private BigDecimal weightCoefficient;
    /**
     * 国内运费单价（每立方米）
     */
    @Schema(description = "国内运费单价（每立方米）", example = "150.00")
    private BigDecimal domesticFreightUnit;
    /**
     * 货代费用单价（每立方米）
     */
    @Schema(description = "货代费用单价（每立方米）", example = "60.00")
    private BigDecimal freightForwarderUnit;
    /**
     * 关税率(%)
     */
    @Schema(description = "关税率(%)", example = "0.00")
    private BigDecimal tariffRate;
    /**
     * VAT税率(%)
     */
    @Schema(description = "VAT税率(%)", example = "0.00")
    private BigDecimal vatRate;
    /**
     * 申报比例(%)
     */
    @Schema(description = "申报比例(%)", example = "30.00")
    private BigDecimal declarationRatio;
    /**
     * 类目佣金率(%)
     */
    @Schema(description = "类目佣金率(%)", example = "15.00")
    private BigDecimal categoryCommissionRate;
    /**
     * 数字服务费率(%)
     */
    @Schema(description = "数字服务费率(%)", example = "2.00")
    private BigDecimal digitalServiceRate;
    /**
     * 是否启用FBA：1-是，0-否
     */
    @Schema(description = "是否启用FBA：1-是，0-否", example = "1")
    private Integer fbaEnabled;
    /**
     * 广告费率(%)
     */
    @Schema(description = "广告费率(%)", example = "0.00")
    private BigDecimal adRate;
    /**
     * 退货率(%)
     */
    @Schema(description = "退货率(%)", example = "2.00")
    private BigDecimal returnRate;
    /**
     * 海运计费方式：1-按体积，2-按重量
     */
    @Schema(description = "海运计费方式：1-按体积，2-按重量", example = "1")
    private Integer shippingCalculationType;
    /**
     * 海运单价
     */
    @Schema(description = "海运单价", example = "0.0000")
    private BigDecimal shippingUnitPrice;
    /**
     * 币种代码
     */
    @Schema(description = "币种代码", example = "USD")
    private String currencyCode;
    
    // 基础产品信息
    /**
     * 产品SKU
     */
    @Schema(description = "产品SKU", example = "ABC-123")
    private String sku;
    /**
     * 产品长度(cm)
     */
    @Schema(description = "产品长度(cm)", example = "10.5")
    private BigDecimal productLength;
    /**
     * 产品宽度(cm)
     */
    @Schema(description = "产品宽度(cm)", example = "8.0")
    private BigDecimal productWidth;
    /**
     * 产品高度(cm)
     */
    @Schema(description = "产品高度(cm)", example = "5.0")
    private BigDecimal productHeight;
    /**
     * 产品重量(kg)
     */
    @Schema(description = "产品重量(kg)", example = "0.5")
    private BigDecimal productWeight;

    
    // 售价和汇率
    /**
     * 售价
     */
    @Schema(description = "售价", example = "29.99")
    private BigDecimal price;
    /**
     * 币种
     */
    @Schema(description = "币种", example = "1")
    private Integer currency;
    /**
     * 汇率
     */
    @Schema(description = "汇率", example = "6.8")
    private BigDecimal exchangeRate;
    
    // 成本输入（用户填写或导入）
    /**
     * 采购单价（CNY，不含税）
     */
    @Schema(description = "采购单价（CNY，不含税）", example = "15.00")
    private BigDecimal purchaseUnit;
    /**
     * 采购成本（目标币种，根据汇率计算）
     */
    @Schema(description = "采购成本（目标币种，根据汇率计算）", example = "2.21")
    private BigDecimal purchaseCost;
    /**
     * 配送费
     */
    @Schema(description = "配送费", example = "2.50")
    private BigDecimal deliveryCost;
    /**
     * 仓储费
     */
    @Schema(description = "仓储费", example = "1.20")
    private BigDecimal storageCost;
    
    // 计算中间结果（系统计算）
    /**
     * 立方米
     */
    @Schema(description = "立方米", example = "0.00042")
    private BigDecimal volumeM3;
    /**
     * 计算重量
     */
    @Schema(description = "计算重量(kg)", example = "0.84")
    private BigDecimal calculateWeight;
    /**
     * 计算体积
     */
    @Schema(description = "计算体积", example = "0.84")
    private BigDecimal calculateVolume;
    /**
     * 实际体积（用于海运费计算）
     */
    @Schema(description = "实际体积", example = "0.84")
    private BigDecimal actualVolume;
    /**
     * 实际重量（用于海运费计算）
     */
    @Schema(description = "实际重量(kg)", example = "0.84")
    private BigDecimal actualWeight;
    
    // 各项费用（系统计算）
    /**
     * 国内运费
     */
    @Schema(description = "国内运费", example = "5.20")
    private BigDecimal localTransportCost;
    /**
     * 货代费用
     */
    @Schema(description = "货代费", example = "8.50")
    private BigDecimal freightForwarderCost;
    /**
     * 关税费用
     */
    @Schema(description = "关税", example = "3.15")
    private BigDecimal tariffCost;
    /**
     * VAT费用
     */
    @Schema(description = "VAT税", example = "5.70")
    private BigDecimal vatCost;
    /**
     * 头程运费
     */
    @Schema(description = "头程运费", example = "12.30")
    private BigDecimal firstMileFreightCost;
    /**
     * 类目佣金费用
     */
    @Schema(description = "类目佣金", example = "4.50")
    private BigDecimal categoryCommissionCost;
    /**
     * 数字服务费用
     */
    @Schema(description = "数字服务费", example = "1.50")
    private BigDecimal digitalServiceCost;
    /**
     * 广告费用
     */
    @Schema(description = "广告费", example = "3.00")
    private BigDecimal adCost;
    /**
     * 退换货费用
     */
    @Schema(description = "退换货费", example = "0.60")
    private BigDecimal returnCost;
    /**
     * 海外仓费用
     */
    @Schema(description = "海外仓费用", example = "2.00")
    private BigDecimal fbsCost;

    // 最终计算结果
    /**
     * 总成本
     */
    @Schema(description = "总成本", example = "59.25")
    private BigDecimal totalCost;
    /**
     * 毛利润
     */
    @Schema(description = "毛利润", example = "14.99")
    private BigDecimal grossProfit;
    /**
     * 毛利率(%)
     */
    @Schema(description = "毛利率(%)", example = "20.18")
    private BigDecimal grossMargin;
    /**
     * 净利润
     */
    @Schema(description = "净利润", example = "14.99")
    private BigDecimal netProfit;
    /**
     * 投资回报率(%)
     */
    @Schema(description = "投资回报率(%)", example = "99.93")
    private BigDecimal roi;
    
    // 配置快照
    /**
     * 配置快照
     */
    @Schema(description = "配置快照")
    private String configSnapshot;
    
}