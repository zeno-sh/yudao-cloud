package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author zeno
 * @Date 2024/1/31
 */
@Data
public class SalesDataReportDTO {

    /**
     * 订单量
     */
    private Integer volumeOrder;
    /**
     * 产品销量
     */
    private Integer volumeProduct;

    /**
     * 广告销售额
     */
    private BigDecimal amountAd;

    /**
     * 广告销量
     */
    private Integer volumeAd;

    /**
     * 销售额
     */
    private BigDecimal amountSales;

    /**
     * 退货量
     */
    private Integer volumeReturns;

    /**
     * 退货费用（逆向物流）
     */
    private BigDecimal costReturnDelivery;

    /**
     * 退货率
     */
    private BigDecimal returnRate;

    /**
     * 类目佣金
     */
    private BigDecimal costCommissions;

    /**
     * 平台服务费（促销、premium、商品滞留费等等）
     */
    private BigDecimal costPlatformService;

    /**
     * 发货费
     */
    private BigDecimal costDelivery;

    /**
     * 广告费
     */
    private BigDecimal costAd;

    /**
     * 营业税
     */
    private BigDecimal costBusinessTax;

    /**
     * 银行关税
     */
    private BigDecimal costBankTax;

    /**
     * 采购成本
     */
    private BigDecimal costPurchase;

    /**
     * 头程费用
     */
    private BigDecimal costFirstLeg;

    /**
     * 海外仓操作费
     */
    private BigDecimal costFbsWarehouse;

    /**
     * 仓储费
     */
    private BigDecimal costStorage;

    /**
     * 毛利润
     */
    private BigDecimal grossProfit;

    /**
     * 毛利率
     */
    private BigDecimal grossMargin;

    /**
     * 投资回报率（ROI）
     */
    private BigDecimal roi;

    /**
     * 店铺租金
     */
    private BigDecimal costRent;

    /**
     * 经营收入
     */
    private BigDecimal incomeOperating;

    /**
     * 广告花费/总销售额
     */
    private BigDecimal acoas;


    /**
     * 广告花费/广告售额
     */
    private BigDecimal acos;
}
