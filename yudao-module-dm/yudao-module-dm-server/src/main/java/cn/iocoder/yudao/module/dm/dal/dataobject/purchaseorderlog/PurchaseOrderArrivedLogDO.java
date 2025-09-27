package cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 采购单到货日志 DO
 *
 * @author Zeno
 */
@TableName("dm_purchase_order_arrived_log")
@KeySequence("dm_purchase_order_arrived_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderArrivedLogDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 采购单ID
     */
    private Long purchaseOrderId;
    /**
     * 采购单item ID
     */
    private Long purchaseOrderItemId;
    /**
     * 到货数量
     */
    private Integer arrivedQuantity;
    /**
     * 备注
     */
    private String remark;

}