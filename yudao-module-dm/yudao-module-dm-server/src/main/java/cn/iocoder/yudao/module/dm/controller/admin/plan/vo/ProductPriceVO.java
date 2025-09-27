package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Zeno
 * @createTime: 2024/04/19 19:01
 */
@Schema(description = "管理后台 - 选品计划 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductPriceVO {

    private Long id;
    /**
     * 产品售价
     */
    private BigDecimal sellingPrice;
    /**
     * 价格策略名称
     */
    private String priceStrategyName;
}
