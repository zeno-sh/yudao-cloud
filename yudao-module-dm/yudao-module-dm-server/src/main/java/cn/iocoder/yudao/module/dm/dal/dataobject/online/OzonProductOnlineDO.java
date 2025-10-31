package cn.iocoder.yudao.module.dm.dal.dataobject.online;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;

/**
 * 在线商品 DO
 *
 * @author Zeno
 */
@TableName(value = "dm_ozon_product_online", autoResultMap = true)
@KeySequence("dm_ozon_product_online_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonProductOnlineDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 门店
     */
    private String clientId;
    /**
     * 本地Sku
     */
    private String skuId;
    /**
     * 平台货号
     */
    private String offerId;
    /**
     * 平台Sku
     */
    private String platformSkuId;
    /**
     * 平台商品ID
     */
    private String productId;
    /**
     * 主图
     */
    private String image;
    /**
     * 是否大宗商品
     */
    private Boolean isKgt;
    /**
     * 补贴价
     */
    private BigDecimal marketingPrice;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 创建时间
     */
    private LocalDateTime createAt;
    /**
     * 本地产品ID
     */
    private Long dmProductId;
    /**
     * 在售状态
     */
    @Deprecated
    private Boolean visible;
    /**
     * 归档
     */
    private Boolean isArchived;
    /**
     * 在售状态
     */
    private Integer status;
}