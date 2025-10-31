package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Data
public class ProductFinancialDataDTO {
    @JSONField(name = "commission_amount")
    private BigDecimal commissionAmount;
    @JSONField(name = "commission_percent")
    private Integer commissionPercent;
    private BigDecimal payout;
    @JSONField(name = "product_id")
    private Long productId;
    private BigDecimal oldPrice;
    private BigDecimal price;
    private BigDecimal totalDiscountValue;
    private BigDecimal totalDiscountPercent;
    private Integer quantity;
    private String clientPrice;
    private String currencyCode;
}
