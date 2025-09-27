package cn.iocoder.yudao.module.dm.dal.dataobject.finance;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 付款单 DO
 *
 * @author Zeno
 */
@TableName(value = "dm_finance_payment", autoResultMap = true)
@KeySequence("dm_finance_payment_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancePaymentDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 付款单号
     */
    private String no;
    /**
     * 审批状态
     */
    private Integer auditStatus;
    /**
     * 合计金额
     */
    private BigDecimal totalPrice;
    /**
     * 实付金额
     */
    private BigDecimal paymentPrice;
    /**
     * 优惠金额
     */
    private BigDecimal discountPrice;
    /**
     * 财务人员
     */
    private String owner;

    /**
     * BPM 流程实例编号
     */
    private String processInstanceId;

    /**
     * 图片数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> picUrls;

}