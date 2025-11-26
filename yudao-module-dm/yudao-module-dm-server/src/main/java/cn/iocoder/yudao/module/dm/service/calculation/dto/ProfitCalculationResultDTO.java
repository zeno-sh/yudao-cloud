package cn.iocoder.yudao.module.dm.service.calculation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 利润测算结果DTO
 *
 * @author Jax
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitCalculationResultDTO {

    // ========== 基础信息 ==========
    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 选品计划名称
     */
    private String planName;

    /**
     * 平台
     */
    private Integer platform;

    /**
     * 国家
     */
    private String country;

    /**
     * 配置模板ID
     */
    private Long templateId;
    
    // 计算配置参数
    /**
     * 体积系数（立方米转重量）
     */
    private BigDecimal volumeCoefficient;
    /**
     * 重量系数（重量转体积）
     */
    private BigDecimal weightCoefficient;
    /**
     * 国内运费单价（每立方米）
     */
    private BigDecimal domesticFreightUnit;
    /**
     * 货代费用单价（每立方米）
     */
    private BigDecimal freightForwarderUnit;
    /**
     * 关税率(%)
     */
    private BigDecimal tariffRate;
    /**
     * VAT税率(%)
     */
    private BigDecimal vatRate;
    /**
     * 销售税率(%)（韩国等国家）
     */
    private BigDecimal saleRate;
    /**
     * 申报比例(%)
     */
    private BigDecimal declarationRatio;
    /**
     * 类目佣金率(%)
     */
    private BigDecimal categoryCommissionRate;
    /**
     * 数字服务费率(%)
     */
    private BigDecimal digitalServiceRate;
    /**
     * 是否启用FBA：1-是，0-否
     */
    private Integer fbaEnabled;
    /**
     * 广告费率(%)
     */
    private BigDecimal adRate;
    /**
     * 退货率(%)
     */
    private BigDecimal returnRate;
    /**
     * 海运计费方式：1-按体积，2-按重量
     */
    private Integer shippingCalculationType;
    /**
     * 海运单价
     */
    private BigDecimal shippingUnitPrice;
    /**
     * 币种代码
     */
    private String currencyCode;
    
    // 基础产品信息
    /**
     * 产品SKU
     */
    private String sku;
    /**
     * 产品长度(cm)
     */
    private BigDecimal productLength;
    /**
     * 产品宽度(cm)
     */
    private BigDecimal productWidth;
    /**
     * 产品高度(cm)
     */
    private BigDecimal productHeight;
    /**
     * 产品重量(kg)
     */
    private BigDecimal productWeight;

    
    // 售价和汇率
    /**
     * 售价
     */
    private BigDecimal price;
    /**
     * 汇率
     */
    private BigDecimal exchangeRate;
    
    // 成本输入（用户填写或导入）
    /**
     * 采购单价（CNY，不含税）
     */
    private BigDecimal purchaseUnit;
    /**
     * 采购价（目标币种，根据汇率计算）
     */
    private BigDecimal purchaseCost;
    /**
     * 配送费
     */
    private BigDecimal deliveryCost;
    /**
     * 仓储费
     */
    private BigDecimal storageCost;

    
    // 计算中间结果（系统计算）
    /**
     * 立方米
     */
    private BigDecimal volumeM3;
    /**
     * 计算重量（用于海运费计算）
     */
    private BigDecimal calculateWeight;
    /**
     * 计算体积（用于海运费计算）
     */
    private BigDecimal calculateVolume;
    /**
     * 实际体积
     */
    private BigDecimal actualVolume;
    /**
     * 实际重量
     */
    private BigDecimal actualWeight;

    
    // 各项费用（系统计算）
    /**
     * 国内运费
     */
    private BigDecimal localTransportCost;
    /**
     * 货代费用
     */
    private BigDecimal freightForwarderCost;
    /**
     * 关税费用
     */
    private BigDecimal tariffCost;
    /**
     * VAT费用
     */
    private BigDecimal vatCost;
    /**
     * 销售税费用（韩国等国家）
     */
    private BigDecimal saleCost;
    /**
     * 应纳税额（韩国：销项税-进项税）
     */
    private BigDecimal actualTaxAmount;
    /**
     * 头程运费
     */
    private BigDecimal firstMileFreightCost;
    /**
     * 类目佣金费用
     */
    private BigDecimal categoryCommissionCost;
    /**
     * 数字服务费用
     */
    private BigDecimal digitalServiceCost;
    /**
     * 广告费用
     */
    private BigDecimal adCost;
    /**
     * 退换货费用
     */
    private BigDecimal returnCost;
    /**
     * 海外仓费用
     */
    private BigDecimal fbsCost;

    // 利润计算结果
    /**
     * 总成本
     */
    private BigDecimal totalCost;
    /**
     * 毛利润
     */
    private BigDecimal grossProfit;
    /**
     * 毛利率(%)
     */
    private BigDecimal grossMargin;
    /**
     * 投资回报率(%)
     */
    private BigDecimal roi;
    /**
     * 净利润
     */
    private BigDecimal netProfit;
    
    /**
     * 配置快照（避免模板变更影响历史数据）
     */
    private String configSnapshot;

}