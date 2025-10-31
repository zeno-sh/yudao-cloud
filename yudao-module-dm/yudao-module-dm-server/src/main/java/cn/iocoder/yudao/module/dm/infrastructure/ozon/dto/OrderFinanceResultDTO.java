package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Data
public class OrderFinanceResultDTO {

    /**
     * 指定期间内商品的总成本和退货。
     */
    @JSONField(name="accruals_for_sale")
    private BigDecimal accrualsForSale;
    /**
     * 商品预售时预扣的佣金数额，退货时返还的佣金数
     */
    @JSONField(name="sale_commission")
    private BigDecimal saleCommission;
    /**
     * 运输处理、订单装配、干线、最后一英里
     */
    @JSONField(name="processing_and_delivery")
    private BigDecimal processingAndDelivery;
    /**
     * 干线返回、退货处理、取消和非赎回
     */
    @JSONField(name="refunds_and_cancellations")
    private BigDecimal refundsAndCancellations;
    /**
     * 与商品交付和退货没有直接关系的附加服务成本。例如，促销或商品放置
     */
    @JSONField(name="services_amount")
    private BigDecimal servicesAmount;
    /**
     * 补贴
     */
    @JSONField(name="compensation_amount")
    private BigDecimal compensationAmount;
    /**
     * 根据“卖方选择交货”计划工作时的交货和退货费用
     */
    @JSONField(name="money_transfer")
    private BigDecimal moneyTransfer;
    /**
     * 其他应计费用
     */
    @JSONField(name="others_amount")
    private BigDecimal othersAmount;
}
