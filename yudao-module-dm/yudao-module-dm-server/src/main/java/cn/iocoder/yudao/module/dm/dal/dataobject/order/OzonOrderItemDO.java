package cn.iocoder.yudao.module.dm.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import lombok.*;

import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 订单商品详情 DO
 *
 * @author Zeno
 */
@TableName("dm_ozon_order_item")
@KeySequence("dm_ozon_order_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonOrderItemDO extends TenantBaseDO {

    /**
     * 自动递增主键
     */
    @TableId
    private Long id;
    /**
     * 平台门店id
     */
    private String clientId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 发货编号
     */
    private String postingNumber;
    /**
     * 平台SkuId
     */
    private String platformSkuId;
    /**
     * 货号
     */
    private String offerId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 商品销售价格
     */
    private BigDecimal price;
    /**
     * 佣金率
     */
    private BigDecimal commissionPercent;
    /**
     * 下单时间
     */
    private LocalDateTime inProcessAt;
    
    /**
     * 本地SkuId - 仅用于SKU查询模式
     * 注意：这不是数据库字段
     */
    @TableField(exist = false)
    private String skuId;
    
    /**
     * 本地产品ID - 仅用于SKU查询模式
     * 注意：这不是数据库字段
     */
    @TableField(exist = false)
    private Long dmProductId;
}