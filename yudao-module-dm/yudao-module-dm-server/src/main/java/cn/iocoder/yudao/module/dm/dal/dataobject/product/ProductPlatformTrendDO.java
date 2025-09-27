package cn.iocoder.yudao.module.dm.dal.dataobject.product;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 产品销量趋势 DO
 *
 * @author Zeno
 */
@TableName("dm_product_platform_trend")
@KeySequence("dm_product_platform_trend_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPlatformTrendDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 产品Id
     */
    private Long productId;
    /**
     * 本地SKU
     */
    private String skuId;
    /**
     * 平台
     */
    private Integer platformId;
    /**
     * 平台销量
     */
    private Integer competitorSaleNumber;
    /**
     * 平台产品链接
     */
    private String competitorLink;
    /**
     * 竞品产品Id
     */
    private String competitorSkuId;
    /**
     * 划线价
     */
    private BigDecimal competitorSalePrice;

}