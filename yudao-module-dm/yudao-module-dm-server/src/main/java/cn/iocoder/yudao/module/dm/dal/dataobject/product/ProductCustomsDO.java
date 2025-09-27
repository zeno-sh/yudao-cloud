package cn.iocoder.yudao.module.dm.dal.dataobject.product;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 海关信息 DO
 *
 * @author Zeno
 */
@TableName("dm_product_customs")
@KeySequence("dm_product_customs_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCustomsDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 产品ID
     */
    private String skuId;
    /**
     * 报关名称英文
     */
    private String customsEn;
    /**
     * 报关名称中文
     */
    private String customsZh;
    /**
     * 材质
     */
    private String material;
    /**
     * 含电池
     */
    private String hasBattery;
    /**
     * 含液体
     */
    private String hasLiquid;
    /**
     * 纺织品
     */
    private String hasTextile;
    /**
     * 报关价格（美元）
     */
    private BigDecimal price;
    /**
     * 产品Id
     */
    private Long productId;

}