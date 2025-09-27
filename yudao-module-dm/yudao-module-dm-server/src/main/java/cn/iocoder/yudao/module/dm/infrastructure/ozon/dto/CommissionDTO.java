package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author zeno
 * @Date 2024/2/15
 */
@Data
public class CommissionDTO {
    /**
     * FBO和FBS方案中最高的销售佣金百分比。
     */
    @JSONField(name = "sales_percent")
    private BigDecimal salesPercent;

    /**
     * FBO履行金额。
     */
    @JSONField(name = "fbo_fulfillment_amount")
    private BigDecimal fboFulfillmentAmount;

    /**
     * FBO直流传输最小金额。
     */
    @JSONField(name = "fbo_direct_flow_trans_min_amount")
    private BigDecimal fboDirectFlowTransMinAmount;

    /**
     * FBO直流传输最大金额。
     */
    @JSONField(name = "fbo_direct_flow_trans_max_amount")
    private BigDecimal fboDirectFlowTransMaxAmount;

    /**
     * FBO交付给客户的金额。
     */
    @JSONField(name = "fbo_deliv_to_customer_amount")
    private BigDecimal fboDelivToCustomerAmount;

    /**
     * FBO返回流量金额。
     */
    @JSONField(name = "fbo_return_flow_amount")
    private BigDecimal fboReturnFlowAmount;

    /**
     * FBO返回流量传输最小金额。
     */
    @JSONField(name = "fbo_return_flow_trans_min_amount")
    private BigDecimal fboReturnFlowTransMinAmount;

    /**
     * FBO返回流量传输最大金额。
     */
    @JSONField(name = "fbo_return_flow_trans_max_amount")
    private BigDecimal fboReturnFlowTransMaxAmount;

    /**
     * 货件处理最低佣金(FBS)——0卢布。
     */
    @JSONField(name = "fbs_first_mile_min_amount")
    private BigDecimal fbsFirstMileMinAmount;

    /**
     * 货件处理最高佣金(FBS)——25卢布。
     */
    @JSONField(name = "fbs_first_mile_max_amount")
    private BigDecimal fbsFirstMileMaxAmount;

    /**
     * 从的主干线。
     */
    @JSONField(name = "fbs_direct_flow_trans_min_amount")
    private BigDecimal fbsDirectFlowTransMinAmount;

    /**
     * 通往的主干线。
     */
    @JSONField(name = "fbs_direct_flow_trans_max_amount")
    private BigDecimal fbsDirectFlowTransMaxAmount;

    /**
     * 最后一英里。
     */
    @JSONField(name = "fbs_deliv_to_customer_amount")
    private BigDecimal fbsDelivToCustomerAmount;

    /**
     * 退款和取消，处理货物的佣金。
     */
    @JSONField(name = "fbs_return_flow_amount")
    private BigDecimal fbsReturnFlowAmount;

    /**
     * 退款和取消的佣金，从的主干线。
     */
    @JSONField(name = "fbs_return_flow_trans_min_amount")
    private BigDecimal fbsReturnFlowTransMinAmount;

    /**
     * 退款和取消的佣金，通往的主干线。
     */
    @JSONField(name = "fbs_return_flow_trans_max_amount")
    private BigDecimal fbsReturnFlowTransMaxAmount;

    /**
     * 销售佣金百分比 (FBO)。
     */
    @JSONField(name = "sales_percent_fbo")
    private BigDecimal salesPercentFbo;

    /**
     * 销售佣金百分比 (FBS)。
     */
    @JSONField(name = "sales_percent_fbs")
    private BigDecimal salesPercentFbs;
}
