package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 产品成本结构 DTO
 *
 * @author Jax
 */
@Data
public class ProductCostsDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 目标平台
     */
    private Integer platform;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 采购成本
     */
    private BigDecimal purchaseCost;

    /**
     * 采购成本币种
     *
     * @deprecated 请使用 {@link #purchaseCurrencyCode} 代替
     */
    @Deprecated
    private Integer purchaseCurrency;

    /**
     * 采购成本币种代码
     */
    private String purchaseCurrencyCode;

    /**
     * 采购运费
     */
    private BigDecimal purchaseShippingCost;

    /**
     * 采购运费计费单位
     */
    private Integer purchaseShippingUnit;

    /**
     * 头程运费
     */
    private BigDecimal logisticsShippingCost;

    /**
     * 头程运费计费单位
     */
    private Integer logisticsUnit;

    /**
     * 头程成本币种
     *
     * @deprecated 请使用 {@link #logisticsCurrencyCode} 代替
     */
    @Deprecated
    private Integer logisticsCurrency;

    /**
     * 头程成本币种代码
     */
    private String logisticsCurrencyCode;

    /**
     * 海关关税费率
     */
    private BigDecimal importVat;

    /**
     * 海关关税费率
     */
    private BigDecimal customsDuty;

    /**
     * 海关申报价
     */
    private BigDecimal customsDeclaredValue;

    /**
     * FBO送仓费
     */
    private BigDecimal fboDeliveryCost;

    /**
     * FBO送仓费计费单位
     */
    private Integer fboDeliveryCostUnit;

    /**
     * FBO验收费
     */
    private BigDecimal fboInspectionCost;

    /**
     * FBO验收费计费单位
     */
    private Integer fboInspectionCostUnit;

    /**
     * 货损率
     */
    private BigDecimal damageRate;

    /**
     * 销售税率
     */
    private BigDecimal salesTaxRate;

    /**
     * 银行提现费率
     */
    private BigDecimal bankWithdrawRate;

    /**
     * 类目佣金ID
     */
    private Long categoryCommissionId;

    /**
     * 收单费率
     */
    private BigDecimal orderFeeRate;

    /**
     * 最后一公里费用
     */
    private BigDecimal platformLogisticsLastMileCost;

    /**
     * 转运费用
     */
    private BigDecimal platformLogisticsTransferCost;

    /**
     * 广告费率
     */
    private BigDecimal adFeeRate;

    /**
     * 海关申报价币种
     *
     * @deprecated 请使用 {@link #customsCurrencyCode} 代替
     */
    @Deprecated
    private Integer customsCurrency;

    /**
     * 海关申报价币种代码
     */
    private String customsCurrencyCode;

    /**
     * FBO送仓费币种
     *
     * @deprecated 请使用 {@link #fboCurrencyCode} 代替
     */
    @Deprecated
    private Integer fboCurrency;

    /**
     * FBO送仓费币种代码
     */
    private String fboCurrencyCode;

    /**
     * 平台成本币种
     *
     * @deprecated 请使用 {@link #platformCurrencyCode} 代替
     */
    @Deprecated
    private Integer platformCurrency;

    /**
     * 平台成本币种代码
     */
    private String platformCurrencyCode;

    /**
     * 海外仓
     */
    private List<Long> fbsWarehouseIds;

    /**
     * 海外仓币种
     *
     * @deprecated 请使用 {@link #fbsCurrencyCode} 代替
     */
    @Deprecated
    private Integer fbsCurrency;

    /**
     * 海外仓币种代码
     */
    private String fbsCurrencyCode;

    /**
     * 海外仓单位
     */
    private Integer fbsCostUnit;

    /**
     * 海外仓成本
     */
    private BigDecimal fbsCost;

} 