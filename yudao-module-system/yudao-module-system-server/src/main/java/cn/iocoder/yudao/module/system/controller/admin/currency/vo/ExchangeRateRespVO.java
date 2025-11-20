package cn.iocoder.yudao.module.system.controller.admin.currency.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 汇率信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ExchangeRateRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer id;

    @Schema(description = "基础货币代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "USD")
    @ExcelProperty("基础货币代码")
    private String baseCurrency;

    @Schema(description = "1单位基础货币=多少人民币", requiredMode = Schema.RequiredMode.REQUIRED, example = "7.2345")
    @ExcelProperty("汇率")
    private BigDecimal rate;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}