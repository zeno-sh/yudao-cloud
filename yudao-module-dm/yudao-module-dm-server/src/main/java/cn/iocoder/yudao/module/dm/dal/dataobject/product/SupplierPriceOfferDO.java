package cn.iocoder.yudao.module.dm.dal.dataobject.product;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 供应商报价 DO
 *
 * @author Zeno
 */
@TableName("dm_supplier_price_offer")
@KeySequence("dm_supplier_price_offer_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPriceOfferDO extends BaseDO {

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
     * 币种
     */
    private Integer currency;
    /**
     * 是否含税
     */
    private String tax;
    /**
     * 税率
     */
    private BigDecimal taxRate;
    /**
     * 报价
     */
    private BigDecimal price;
    /**
     * 报价
     */
    private BigDecimal taxPrice;
    /**
     * 起订数量
     */
    private Integer orderNumber;
    /**
     * 采购链接
     */
    private String link;
    /**
     * 交期
     */
    private Integer deliveryTime;
    /**
     * 首选
     */
    private String firstChoice;
    /**
     * 备注
     */
    private String remark;
    /**
     * 报价时间
     */
    private String offerDate;
    /**
     * 产品Id
     */
    private Long productId;

}