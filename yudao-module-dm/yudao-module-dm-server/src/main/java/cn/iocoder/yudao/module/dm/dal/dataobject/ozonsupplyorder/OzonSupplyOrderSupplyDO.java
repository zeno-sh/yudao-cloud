package cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 供应订单供应关系 DO
 *
 * @author Zeno
 */
@TableName("dm_ozon_supply_order_supply")
@KeySequence("dm_ozon_supply_order_supply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonSupplyOrderSupplyDO extends BaseDO {

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
     * Ozon供应订单ID
     */
    private Long supplyOrderId;
    /**
     * 供应ID
     */
    private String supplyId;
    /**
     * 包裹ID
     */
    private String bundleId;
    /**
     * 存储仓库ID
     */
    private Long storageWarehouseId;
} 