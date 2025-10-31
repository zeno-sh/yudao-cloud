package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 商品成本
 * 所有的成本都会换算成 RMB
 *
 * @Author zeno
 * @Date 2024/2/1
 */
@Data
public class ProductCostDTO {

    /**
     * 门店ID
     */
    private String clientId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 货号
     */
    private String offerId;

    /**
     * 平台SkuId
     */
    private String platformSkuId;

    // ============收入===========
    /**
     * 销售金额
     */
    private BigDecimal saleAmount;

    /**
     * 平台结算金额=售价-佣金-配送费
     */
    private BigDecimal settleAmount;

    /**
     * 逆向结算，主要来自取消
     */
    private BigDecimal refundSettleAmount;

    /**
     * 订单量
     */
    private Integer orders;

    /**
     * 销量
     */
    private Integer salesVolume;

    /**
     * 退还佣金
     */
    private BigDecimal returnCommissionAmount;

    // ============采购===========
    /**
     * 采购成本(含税价)
     */
    private BigDecimal purchaseCost;
    /**
     * 采购运费
     */
    private BigDecimal purchaseShippingCost;

    // ============头程===========
    /**
     * 头程运费
     */
    private BigDecimal logisticsShippingCost;

    // ============海关===========
    /**
     * 进口VAT=(申报货值+关税金额)*20%
     */
    private BigDecimal vatCost;
    /**
     * 关税金额=申报货值*关税税率
     */
    private BigDecimal customsCost;
    /**
     * 申报货值
     */
    private BigDecimal declaredValueCost;

    // ============ FBS ===========
    /**
     * 入仓前费用
     */
    private BigDecimal fbsCheckInCost;
    /**
     * 出单后操作费
     */
    private BigDecimal fbsOperatingCost;
    /**
     * 其他：售后等
     */
    private BigDecimal fbsOtherCost;

    // ============ 平台 ===========
    /**
     * 类目佣金
     */
    private BigDecimal categoryCommissionCost;
    /**
     * 收单费额
     */
    private BigDecimal orderFeeCost;
    /**
     * 最后一公里费用
     */
    private BigDecimal platformLogisticsLastMileCost;
    /**
     * 转运费用
     */
    private BigDecimal platformLogisticsTransferCost;
    /**
     * drop off
     */
    private BigDecimal platformLogisticsDropOffCost;
    /**
     * 退货/取消 金额
     */
    private BigDecimal cancelledAmount;
    /**
     * 逆向物流
     */
    private BigDecimal reverseLogisticsCost;
    /**
     * 退款、赔偿
     */
    private BigDecimal refundAmount;

    // ============= 产品维度结算时不统计 ===============
    /**
     * 广告花费
     */
    private BigDecimal adCost;
    /**
     * 广告销售额
     */
    private BigDecimal adSaleAmount;
    /**
     * 广告销量
     */
    private Integer adOrders;
    // ================================================
    /**
     * 平台其他服务费
     */
    private BigDecimal platformServiceCost;
    /**
     * FBO送仓费
     */
    private BigDecimal fboDeliveryCost;
    /**
     * FBO验收费
     */
    private BigDecimal fboInspectionCost;

    // ============ 税务 ===========
    /**
     * 销售VAT
     */
    private BigDecimal salesVatCost;

    // ============ 其他 ===========
    /**
     * 货损
     */
    private BigDecimal damageCost;

    /**
     * 利润=清关申报货值+提现金额+退税金额-采购含税价-头程-海外仓费用
     */
    private BigDecimal profitAmount;

    /**
     * 提示信息
     */
    private List<String> errorMsg = new ArrayList<>();

    /**
     * 退货/取消 订单统计
     * key: posting_number
     */
    private Set<String> cancelledOrders;

    /**
     * 平台费用币种
     */
    private Integer platformCurrency;

    /**
     * FBS费用币种
     */
    private Integer fbsCurrency;

    /**
     * 采购成本币种
     */
    private Integer purchaseCurrency;

    /**
     * 头程币种
     */
    private Integer logisticsCurrency;

    /**
     *  海关币种
     */
    private  Integer customsCurrency;

}
