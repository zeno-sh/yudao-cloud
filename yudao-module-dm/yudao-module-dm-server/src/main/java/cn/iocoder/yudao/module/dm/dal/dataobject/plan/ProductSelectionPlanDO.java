package cn.iocoder.yudao.module.dm.dal.dataobject.plan;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 选品计划 DO
 *
 * @author Zeno
 */
@TableName("dm_product_selection_plan")
@KeySequence("dm_product_selection_plan_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSelectionPlanDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 选品计划编号
     */
    private String planCode;
    /**
     * 选品计划名称
     */
    private String planName;
    /**
     * SKU
     */
    private String planSkuId;
    /**
     * 价格计划
     */
    private Long priceId;
    /**
     * 供应商报价
     */
    private Long supplierPriceOfferId;
    /**
     * 货代报价（人民币）
     */
    private BigDecimal forwarderPrice;
    /**
     * 手动预估采购价
     */
    private BigDecimal forecastPurchasePrice;
    /**
     * 广告费率
     */
    private BigDecimal adRate;
    /**
     * 货损率
     */
    private BigDecimal lossRate;
    /**
     * 本地产品ID
     */
    private Long productId;

}