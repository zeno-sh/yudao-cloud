package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/8
 */
@Data
public class ProductOnlineItemDTO {

    @JSONField(name = "offer_id")
    private String offerId;
    @JSONField(name = "product_id")
    private String productId;
}
