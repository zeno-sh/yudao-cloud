package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author zeno
 * @Date 2024/2/15
 */
@Data
public class PriceDTO {

    private BigDecimal price;
    @JSONField(name = "marketing_seller_price")
    private BigDecimal marketingSellerPrice;
}
