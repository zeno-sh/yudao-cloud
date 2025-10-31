package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * 货币金额（支持多币种）
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 货币金额")
@Data
@Builder
public class MonetaryAmount {
    
    @Schema(description = "金额", example = "1000.00")
    private BigDecimal amount;
    
    @Schema(description = "币种代码", example = "CNY")
    private String currencyCode;
    
    @Schema(description = "币种", example = "人民币")
    private String currencyName;
    
    /**
     * 创建人民币金额
     */
    public static MonetaryAmount ofCNY(BigDecimal amount) {
        return MonetaryAmount.builder()
            .amount(amount)
            .currencyCode("CNY")
            .currencyName("人民币")
            .build();
    }
    
    /**
     * 创建卢布金额
     */
    public static MonetaryAmount ofRUB(BigDecimal amount) {
        return MonetaryAmount.builder()
            .amount(amount)
            .currencyCode("RUB")
            .currencyName("卢布")
            .build();
    }
    
    /**
     * 创建美元金额
     */
    public static MonetaryAmount ofUSD(BigDecimal amount) {
        return MonetaryAmount.builder()
            .amount(amount)
            .currencyCode("USD")
            .currencyName("美元")
            .build();
    }
} 