package cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 采购单明细 DO
 *
 * @author Zeno
 */
@TableName("dm_purchase_order_item")
@KeySequence("dm_purchase_order_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 采购单Id
     */
    private Long orderId;
    /**
     * 产品ID
     */
    private Long productId;
    /**
     * 单箱数量
     */
    private Integer pcs;
    /**
     * 单价
     */
    private BigDecimal price;
    /**
     * 含税单价
     */
    private BigDecimal taxPrice;
    /**
     * 税率
     */
    private BigDecimal taxRate;
    /**
     * 含税
     */
    private Boolean tax;
    /**
     * 采购数量
     */
    private Integer purchaseQuantity;
    /**
     * 预计到货时间
     */
    private LocalDateTime arrivalDate;
    /**
     * 备注
     */
    private String remark;

    /**
     * 税额（单价*税率*数量）
     */
    private BigDecimal taxAmount;
    /**
     * 总金额（单价*采购数量）
     */
    private BigDecimal totalAmount;
    /**
     * 含税总金额
     */
    private BigDecimal totalTaxAmount;
    /**
     * 采购计划编号
     */
    private String planNumber;
    /**
     * 主表的采购状态
     */
    private Integer status;
}