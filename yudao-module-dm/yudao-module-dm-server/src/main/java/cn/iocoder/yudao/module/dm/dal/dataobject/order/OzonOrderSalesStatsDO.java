package cn.iocoder.yudao.module.dm.dal.dataobject.order;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Ozon 订单销售统计 DO
 */
@Data
@Accessors(chain = true)
public class OzonOrderSalesStatsDO {

    /**
     * 月份，格式为yyyy-MM
     */
    private String month;

    /**
     * 商品Offer ID
     */
    private String offerId;

    /**
     * 销售数量
     */
    private Integer salesQuantity;

} 