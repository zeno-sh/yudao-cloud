package cn.iocoder.yudao.module.system.controller.admin.currency.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 币种信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CurrencyRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer id;

    @Schema(description = "ISO货币代码(3位字母)", requiredMode = Schema.RequiredMode.REQUIRED, example = "USD")
    @ExcelProperty("货币代码")
    private String currencyCode;

    @Schema(description = "货币名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "美元")
    @ExcelProperty("货币名称")
    private String name;

    @Schema(description = "货币符号", example = "$")
    @ExcelProperty("货币符号")
    private String symbol;

}