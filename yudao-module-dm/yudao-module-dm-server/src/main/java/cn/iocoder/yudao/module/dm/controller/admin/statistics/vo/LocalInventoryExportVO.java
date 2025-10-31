package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocalInventoryExportVO {

    @ExcelProperty("时间")
    private String date;

    @ExcelProperty("产品ID")
    private Long productId;

    @ExcelProperty("SKU")
    private String skuId;

    @ExcelProperty("SKU名称")
    private String skuName;

    @ExcelProperty("期初库存")
    private Integer preTotal;

    @ExcelProperty("本期采购")
    private Integer currentTotal;

    @ExcelProperty("本期发货")
    private Integer currentTotalDeliver;

    @ExcelProperty("本期结余")
    private Integer currentTotalBalance;

    @ExcelProperty("库存货值(不含税)")
    private BigDecimal currentTotalPrice;

    @ExcelProperty("库存货值(含税)")
    private BigDecimal currentTotalTaxPrice;

}