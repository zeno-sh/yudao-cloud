package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

/**
 * @author: Zeno
 * @createTime: 2024/10/16 17:02
 */
@Data
public class ProfitReportRefreshReqVO {

    @Schema(description = "门店ID", example = "7216")
    private String[] clientIds;

    @Schema(description = "账单日期")
    private String[] financeDate;
    
    @Schema(description = "查询维度：sku、client，默认为client", example = "client")
    private String dimension = "client";
    
    @Schema(description = "时间类型：day、week、month，默认为day", example = "day")
    private String timeType = "day";
}
