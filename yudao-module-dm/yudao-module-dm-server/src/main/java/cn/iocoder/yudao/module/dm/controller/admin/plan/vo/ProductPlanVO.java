package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/04/19 17:20
 */
@Schema(description = "管理后台 - 选品计划 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductPlanVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1592")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "选品计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("选品计划名称")
    private String planName;

    @Schema(description = "本地产品ID", example = "12075")
    @ExcelProperty("本地产品ID")
    private Long productId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "结算价", example = "12075")
    @ExcelProperty("结算价")
    private String settlementPrice;

    @Schema(description = "利润", example = "12075")
    @ExcelProperty("利润")
    private String profitPrice;

    @Schema(description = "毛利率", example = "35%")
    @ExcelProperty("毛利率")
    private String grossProfitRate;

    @Schema(description = "ROI", example = "90%")
    @ExcelProperty("ROI")
    private String roiRate;

    private ProductInfoVO productInfo;

    private ProductCostVO productCost;

    private ProductPriceVO productPrice;

    private List<ProductPlatformTrendVO> productPlatformTrendList;
}
