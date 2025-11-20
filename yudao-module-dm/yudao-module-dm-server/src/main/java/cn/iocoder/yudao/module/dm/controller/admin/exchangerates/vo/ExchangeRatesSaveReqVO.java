package cn.iocoder.yudao.module.dm.controller.admin.exchangerates.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 汇率新增/修改 Request VO")
@Data
public class ExchangeRatesSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22724")
    private Long id;

    @Schema(description = "官方汇率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "官方汇率不能为空")
    private BigDecimal officialRate;

    @Schema(description = "自定义汇率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "自定义汇率不能为空")
    private BigDecimal customRate;

    @Schema(description = "币种", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "币种不能为空")
    private String currencyCode;

}