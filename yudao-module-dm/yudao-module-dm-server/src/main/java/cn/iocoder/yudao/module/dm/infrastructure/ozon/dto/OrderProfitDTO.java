package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单利润
 *
 * @author: Zeno
 * @createTime: 2024/09/24 17:43
 */
@Data
public class OrderProfitDTO {


    /**
     * 门店
     */
    private String clientId;

    /**
     * 本地订单id
     */
    private Long dmOrderId;

    /**
     * 利润
     */
    private BigDecimal amountProfit;

    /**
     * 毛利率
     */
    private BigDecimal profitRate;

    /**
     * roi=利润/(采购成本+采购运费+头程运费)
     */
    private BigDecimal roi;

    /**
     * 成本明细
     */
    private ProductCostDTO productCostDTO;

    // =========== 签收后 ============
    /**
     * 广告销售额
     */
    private BigDecimal amountAd;

    /**
     * 广告销量
     */
    private Integer volumeAd;

    /**
     * 退货量
     */
    private Integer volumeReturn;

    /**
     * 退货金额
     */
    private BigDecimal amountReturn;
}
