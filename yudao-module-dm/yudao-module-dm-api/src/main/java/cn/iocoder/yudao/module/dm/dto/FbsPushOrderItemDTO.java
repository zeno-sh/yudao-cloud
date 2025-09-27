package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/08/27 10:48
 */
@Data
public class FbsPushOrderItemDTO {
    /**
     * 商品的SKU标识
     */
    private String skuId;

    /**
     * 购买商品的数量
     */
    private Integer quantity;

}
