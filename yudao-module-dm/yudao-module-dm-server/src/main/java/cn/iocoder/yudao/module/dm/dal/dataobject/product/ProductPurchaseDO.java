package cn.iocoder.yudao.module.dm.dal.dataobject.product;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 采购信息 DO
 *
 * @author Zeno
 */
@TableName("dm_product_purchase")
@KeySequence("dm_product_purchase_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPurchaseDO extends BaseDO {

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
     * 供应商代码
     */
    private String supplierCode;
    /**
     * 箱规名称
     */
    private String cartonSizeName;
    /**
     * 单品长（cm）
     */
    private BigDecimal length;
    /**
     * 单品宽（cm）
     */
    private BigDecimal width;
    /**
     * 单品高（cm）
     */
    private BigDecimal height;
    /**
     * 单品净重量（g）
     */
    private BigDecimal netWeight;
    /**
     * 产品材质
     */
    private String material;
    /**
     * 箱规长（cm）
     */
    private BigDecimal boxLength;
    /**
     * 箱规宽（cm）
     */
    private BigDecimal boxWidth;
    /**
     * 箱规高（cm）
     */
    private BigDecimal boxHeight;
    /**
     * 每箱子的产品数（pcs）
     */
    private Integer quantityPerBox;
    /**
     * 箱重（g）
     */
    private BigDecimal boxWeight;
    /**
     * 单品毛重量（g）
     */
    private BigDecimal grossWeight;
    /**
     * 首选
     */
    private String firstChoice;
    /**
     * 产品Id
     */
    private Long productId;

}