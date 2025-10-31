package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

/**
 * @author: Zeno
 * @createTime: 2024/07/03 17:06
 */
@Data
public class TradeTrendSummaryRespVO {

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-12-16")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate date;

    @Schema(description = "订单量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer totalOrders;

    @Schema(description = "销售额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private BigDecimal totalSales;

}
