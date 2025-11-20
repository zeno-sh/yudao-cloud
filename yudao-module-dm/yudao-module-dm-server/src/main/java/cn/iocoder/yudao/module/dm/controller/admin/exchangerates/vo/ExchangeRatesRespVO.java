package cn.iocoder.yudao.module.dm.controller.admin.exchangerates.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 汇率 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ExchangeRatesRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22724")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "官方汇率", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("官方汇率")
    private BigDecimal officialRate;

    @Schema(description = "自定义汇率", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("自定义汇率")
    private BigDecimal customRate;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "币种", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currency;

}