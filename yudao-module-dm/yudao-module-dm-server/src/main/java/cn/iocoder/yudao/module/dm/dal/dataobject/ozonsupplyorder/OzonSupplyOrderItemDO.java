package cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 供应订单商品 DO
 *
 * @author Zeno
 */
@TableName("dm_ozon_supply_order_item")
@KeySequence("dm_ozon_supply_order_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonSupplyOrderItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * Ozon客户端ID
     */
    private String clientId;
    /**
     * 供应订单ID
     */
    private Long supplyOrderId;
    /**
     * 包裹ID
     */
    private String bundleId;
    /**
     * 商品SKU
     */
    private Long sku;
    /**
     * 商品ID
     */
    private Long productId;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品数量
     */
    private Integer quantity;
    /**
     * 每包数量
     */
    private Integer quant;
    /**
     * 商品条码
     */
    private String barcode;
    /**
     * 单品体积(升)
     */
    private BigDecimal volume;
    /**
     * 总体积(升)
     */
    private BigDecimal totalVolume;
    /**
     * 卖家商品编码
     */
    private String contractorItemCode;
    /**
     * Super商品属性
     */
    private String sfboAttribute;
    /**
     * 配送类型
     */
    private String shipmentType;
    /**
     * 商品Offer ID
     */
    private String offerId;
    /**
     * 商品图片路径
     */
    private String iconPath;
    /**
     * 本地产品ID
     */
    private Long dmProductId;

}