package cn.iocoder.yudao.module.dm.controller.admin.template.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 利润计算配置模板 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProfitCalculationTemplateRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6116")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("模板名称")
    private String templateName;

    @Schema(description = "国家", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("国家")
    private String country;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("平台")
    private Integer platform;

    @Schema(description = "体积系数（立方米转重量）")
    @ExcelProperty("体积系数（立方米转重量）")
    private BigDecimal volumeCoefficient;

    @Schema(description = "重量系数（重量转体积）")
    @ExcelProperty("重量系数（重量转体积）")
    private BigDecimal weightCoefficient;

    @Schema(description = "国内运费单价（每立方米）")
    @ExcelProperty("国内运费单价（每立方米）")
    private BigDecimal domesticFreightUnit;

    @Schema(description = "货代费用单价（每立方米）")
    @ExcelProperty("货代费用单价（每立方米）")
    private BigDecimal freightForwarderUnit;

    @Schema(description = "关税率(%)")
    @ExcelProperty("关税率(%)")
    private BigDecimal tariffRate;

    @Schema(description = "VAT税率(%)")
    @ExcelProperty("VAT税率(%)")
    private BigDecimal vatRate;

    @Schema(description = "申报比例(%)")
    @ExcelProperty("申报比例(%)")
    private BigDecimal declarationRatio;

    @Schema(description = "类目佣金率(%)")
    @ExcelProperty("类目佣金率(%)")
    private BigDecimal categoryCommissionRate;

    @Schema(description = "数字服务费率(%)（英国等）")
    @ExcelProperty("数字服务费率(%)（英国等）")
    private BigDecimal digitalServiceRate;

    @Schema(description = "是否启用FBA：1-是，0-否")
    @ExcelProperty("是否启用FBA：1-是，0-否")
    private Integer fbaEnabled;

    @Schema(description = "广告费率(%)")
    @ExcelProperty("广告费率(%)")
    private BigDecimal adRate;

    @Schema(description = "退货率(%)")
    @ExcelProperty("退货率(%)")
    private BigDecimal returnRate;

    @Schema(description = "海运计费方式：1-按体积，2-按重量", example = "2")
    @ExcelProperty("海运计费方式：1-按体积，2-按重量")
    private Integer shippingCalculationType;

    @Schema(description = "海运单价", example = "15220")
    @ExcelProperty("海运单价")
    private BigDecimal shippingUnitPrice;

    @Schema(description = "默认币种代码")
    @ExcelProperty("默认币种代码")
    private String currencyCode;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}