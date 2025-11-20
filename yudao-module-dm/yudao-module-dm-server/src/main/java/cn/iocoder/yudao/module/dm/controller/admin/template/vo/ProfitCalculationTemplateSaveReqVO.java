package cn.iocoder.yudao.module.dm.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 利润计算配置模板新增/修改 Request VO")
@Data
public class ProfitCalculationTemplateSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6116")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "模板名称不能为空")
    private String templateName;

    @Schema(description = "国家", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "国家不能为空")
    private String country;

    @Schema(description = "体积系数（立方米转重量）")
    private BigDecimal volumeCoefficient;

    @Schema(description = "重量系数（重量转体积）")
    private BigDecimal weightCoefficient;

    @Schema(description = "国内运费单价（每立方米）")
    private BigDecimal domesticFreightUnit;

    @Schema(description = "货代费用单价（每立方米）")
    private BigDecimal freightForwarderUnit;

    @Schema(description = "关税率(%)")
    private BigDecimal tariffRate;

    @Schema(description = "VAT税率(%)")
    private BigDecimal vatRate;

    @Schema(description = "申报比例(%)")
    private BigDecimal declarationRatio;

    @Schema(description = "类目佣金率(%)")
    private BigDecimal categoryCommissionRate;

    @Schema(description = "数字服务费率(%)（英国等）")
    private BigDecimal digitalServiceRate;

    @Schema(description = "是否启用FBA：1-是，0-否")
    private Integer fbaEnabled;

    @Schema(description = "广告费率(%)")
    private BigDecimal adRate;

    @Schema(description = "退货率(%)")
    private BigDecimal returnRate;

    @Schema(description = "海运计费方式：1-按体积，2-按重量", example = "2")
    private Integer shippingCalculationType;

    @Schema(description = "海运单价", example = "15220")
    private BigDecimal shippingUnitPrice;

    @Schema(description = "默认币种代码")
    private String currencyCode;

}