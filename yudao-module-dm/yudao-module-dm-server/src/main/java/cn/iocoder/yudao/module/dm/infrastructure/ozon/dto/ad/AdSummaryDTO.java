package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdSummaryDTO {

    /**
     * 广告花费
     */
    private BigDecimal moneySpent;
    /**
     * 订单量
     */
    private Integer adOrders;
    /**
     * 广告销售额
     */
    private BigDecimal ordersMoney;

    public static AdSummaryDTO combine(AdSummaryDTO a, AdSummaryDTO b) {
        return new AdSummaryDTO(a.moneySpent.add(b.moneySpent), a.adOrders + b.adOrders, a.ordersMoney.add(b.ordersMoney));
    }
}
