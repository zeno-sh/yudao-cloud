package cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OzonFboSalesStatsDO {
    
    /**
     * 本地产品ID
     */
    private Long dmProductId;
    
    /**
     * 销售数量
     */
    private Integer salesQuantity;
} 