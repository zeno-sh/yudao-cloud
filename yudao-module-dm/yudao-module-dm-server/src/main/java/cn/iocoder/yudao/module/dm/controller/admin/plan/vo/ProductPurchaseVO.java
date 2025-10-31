package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Zeno
 * @createTime: 2024/04/19 17:29
 */
@Schema(description = "管理后台 - 选品计划 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductPurchaseVO {

    private Long id;
    /**
     * 单品长（cm）
     */
    private BigDecimal length;
    /**
     * 单品宽（cm）
     */
    private BigDecimal width;
    /**
     * 单品高（cm）
     */
    private BigDecimal height;
    /**
     * 箱规长（cm）
     */
    private BigDecimal boxLength;
    /**
     * 箱规宽（cm）
     */
    private BigDecimal boxWidth;
    /**
     * 箱规高（cm）
     */
    private BigDecimal boxHeight;
    /**
     * 每箱子的产品数（pcs）
     */
    private Integer quantityPerBox;
    /**
     * 箱重（g）
     */
    private BigDecimal boxWeight;
    /**
     * 单品毛重量（g）
     */
    private BigDecimal grossWeight;
}
