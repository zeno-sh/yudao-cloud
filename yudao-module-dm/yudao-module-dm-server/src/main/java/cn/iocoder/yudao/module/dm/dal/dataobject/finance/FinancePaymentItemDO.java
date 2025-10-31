package cn.iocoder.yudao.module.dm.dal.dataobject.finance;

import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * ERP 付款项 DO
 *
 * @author Zeno
 */
@TableName("dm_finance_payment_item")
@KeySequence("dm_finance_payment_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancePaymentItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
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