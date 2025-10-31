package cn.iocoder.yudao.module.dm.dal.dataobject.product;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 产品价格策略 DO
 *
 * @author Zeno
 */
@TableName("dm_product_price")
@KeySequence("dm_product_price_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * SKU
     */
    private String skuId;
    /**
     * 产品售价
     */
    private BigDecimal sellingPrice;
    /**
     * 产品原价
     */
    private BigDecimal originalPrice;
    /**
     * 价格策略名称
     */
    private String priceStrategyName;
    /**
     * 首选
     */
    private String firstChoice;
    /**
     * 产品Id
     */
    private Long productId;

}