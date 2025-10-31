package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/15
 */
@Data
public class ProductPriceItemDTO {

    @JSONField(name = "product_id")
    private Long productId;

    @JSONField(name = "offer_id")
    private String offerId;

    @JSONField(name = "price_index")
    private String priceIndex;

    @JSONField(name = "commissions")
    private CommissionDTO commissions;

    private PriceDTO price;

}
