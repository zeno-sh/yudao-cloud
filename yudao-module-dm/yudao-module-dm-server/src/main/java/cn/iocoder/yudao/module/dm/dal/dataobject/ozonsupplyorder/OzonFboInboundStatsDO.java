package cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Ozon FBO进仓统计 DO
 */
@Data
@Accessors(chain = true)
public class OzonFboInboundStatsDO {

    /**
     * 商品ID
     */
    private Long dmProductId;

    /**
     * 进仓数量
     */
    private Integer inboundQuantity;

} 