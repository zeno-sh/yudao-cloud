package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/12/27 20:32
 */
@Data
public class ProductStockInfoVO {

    private Long productId;
    /**
     * 统计日期 yyyy-MM-dd
     */
    private String date;
    /**
     * 采购数量
     */
    private Integer purchaseNum;
    /**
     * 取消数量
     */
    private Integer purchaseCancelledNum;
    /**
     * 发货数量
     */
    private Integer deliverNum;
}
