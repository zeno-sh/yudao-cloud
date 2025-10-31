package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/08/29 15:39
 * 
 */
@Data
public class FbsProductStockDTO {

    /**
     * 仓库商品编码
     */
    private String fbsSku;
    /**
     * 在途数量
     */
    private Integer onway;
    /**
     * 待上架数量
     */
    private Integer pending;
    /**
     * 可售数量
     */
    private Integer sellable;
    /**
     * 历史出库数量
     */
    private Integer shipped;


}
