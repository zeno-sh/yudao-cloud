package cn.iocoder.yudao.module.dm.dal.dataobject.warehouse;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 海外仓产品库存 DO
 *
 * @author Zeno
 */
@TableName("dm_fbs_product_stock")
@KeySequence("dm_fbs_product_stock_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbsProductStockDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 仓库ID
     */
    private Long warehouseId;
    /**
     * 仓库Sku
     */
    private String productSku;
    /**
     * 本地产品ID
     */
    private Long productId;
    /**
     * 在途数量
     */
    private Integer onway;
    /**
     * 可售数量
     */
    private Integer sellable;
    /**
     * 历史出库数量
     */
    private Integer shipped;

}