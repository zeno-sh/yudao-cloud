package cn.iocoder.yudao.module.dm.controller.admin.finance.vo;

import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Zeno
 * @createTime: 2024/05/28 16:08
 */
@Schema(description = "管理后台 - 付款单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FinancePaymentItemRespVO {

    private Long id;
    /**
     * 付款单编号
     */
    private Long paymentId;
    /**
     * 业务类型
     */
    private Integer bizType;
    /**
     * 业务编号
     * @see PurchaseOrderDO#getId()
     */
    private Long bizId;
    /**
     * 业务单号
     */
    private String bizNo;
    /**
     * 应付欠款，单位：元
     */
    private BigDecimal totalPrice;
    /**
     * 已付欠款，单位：元
     */
    private BigDecimal paidPrice;
    /**
     * 本次付款，单位：元
     */
    private BigDecimal paymentPrice;
    /**
     * 优惠金额，单位：元
     */
    private BigDecimal discountPrice;
    /**
     * 备注
     */
    private String remark;
}
