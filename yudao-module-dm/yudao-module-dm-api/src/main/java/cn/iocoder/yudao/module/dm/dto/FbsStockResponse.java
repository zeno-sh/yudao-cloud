package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/28 20:42
 */
@Data
public class FbsStockResponse extends ServiceResponse{

    /**
     * 商品库存
     */
    private List<FbsProductStockDTO> stockItems;
}
