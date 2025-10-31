package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 选品计划 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductSelectionPlanRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1592")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "选品计划编号")
    @ExcelProperty("选品计划编号")
    private String planCode;

    @Schema(description = "选品计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("选品计划名称")
    private String planName;

    @Schema(description = "SKU", requiredMode = Schema.RequiredMode.REQUIRED, example = "30509")
    @ExcelProperty("SKU")
    private String planSkuId;

    @Schema(description = "价格计划", example = "10055")
    @ExcelProperty("价格计划")
    private Long priceId;

    @Schema(description = "供应商报价", example = "29694")
    @ExcelProperty("供应商报价")
    private Long supplierPriceOfferId;

    @Schema(description = "货代报价（人民币）", example = "12688")
    @ExcelProperty("货代报价（人民币）")
    private BigDecimal forwarderPrice;

    @Schema(description = "手动预估采购价", example = "2984")
    @ExcelProperty("手动预估采购价")
    private BigDecimal forecastPurchasePrice;

    @Schema(description = "广告费率")
    @ExcelProperty("广告费率")
    private BigDecimal adRate;

    @Schema(description = "货损率")
    @ExcelProperty("货损率")
    private BigDecimal lossRate;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "本地产品ID", example = "12075")
    @ExcelProperty("本地产品ID")
    private Long productId;

}