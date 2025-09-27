package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Data
public class ProductDTO {
    private BigDecimal price;
    @JSONField(name = "offer_id")
    private String offerId;
    private String name;
    private Long sku;
    private Integer quantity;
//    private List<String> mandatoryMark;
    private String currencyCode;
}
