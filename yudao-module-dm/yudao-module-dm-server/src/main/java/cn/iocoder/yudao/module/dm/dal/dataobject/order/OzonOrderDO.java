package cn.iocoder.yudao.module.dm.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * Ozon订单 DO
 *
 * @author Zeno
 */
@TableName(value = "dm_ozon_order")
@KeySequence("dm_ozon_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonOrderDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 平台门店id
     */
    private String clientId;
    /**
     * 平台订单id
     */
    private String orderId;
    /**
     * 发货编号
     */
    private String postingNumber;
    /**
     * 父发货编号
     */
    private String parentPostingNumber;
    /**
     * 订单编号
     */
    private String orderNumber;
    /**
     * 订单状态
     */
    private String status;
    /**
     * 接单时间
     */
    private LocalDateTime inProcessAt;
    /**
     * 发运时间
     */
    private LocalDateTime shipmentDate;
    /**
     * 交货时间
     */
    private LocalDateTime deliveringDate;
    /**
     * 订单销售金额
     */
    private BigDecimal accrualsForSale;
    /**
     * 取消原因
     */
    private String cancellation;
    /**
     * 商品快照
     */
    private String products;
    /**
     * 仓库信息
     */
    private String deliveryMethod;
    /**
     * 订单子状态
     */
    private String substatus;
    /**
     * 发货条码
     */
    private String barcode;
    /**
     * 订单类型
     */
    private Integer orderType;

}