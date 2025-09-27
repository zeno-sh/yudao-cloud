package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/12/25 20:38
 */
@Data
public class ReportStockRespVO {

    /**
     * 日期列表
     */
    private List<String> dateList;
    /**
     * 商品库存列表
     */
    private List<ProductStockVO> productStockList;

}
