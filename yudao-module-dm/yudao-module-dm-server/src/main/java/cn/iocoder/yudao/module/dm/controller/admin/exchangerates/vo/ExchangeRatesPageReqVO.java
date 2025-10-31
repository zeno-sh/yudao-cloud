package cn.iocoder.yudao.module.dm.controller.admin.exchangerates.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 汇率分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExchangeRatesPageReqVO extends PageParam {

    @Schema(description = "基础货币代码（ISO标准）")
    private Integer baseCurrency;

    @Schema(description = "目标货币代码（ISO标准）")
    private Integer targetCurrency;

    @Schema(description = "官方汇率")
    private BigDecimal officialRate;

    @Schema(description = "自定义汇率")
    private BigDecimal customRate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}