package cn.iocoder.yudao.module.dm.service.purchase.order.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 产品库存统计 DTO
 *
 * @author: Zeno
 * @createTime: 2024/12/25 20:48
 */
@Data
public class ProductStockDTO {

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 统计日期
     */
    private String date;

    /**
     * 期初库存
     */
    private Integer preTotal;

    /**
     * 采购数量
     */
    private Integer purchaseNum;

    /**
     * 发货数量
     */
    private Integer deliveryNum;

    /**
     * 本期结余
     */
    private Integer currentBalance;

}
