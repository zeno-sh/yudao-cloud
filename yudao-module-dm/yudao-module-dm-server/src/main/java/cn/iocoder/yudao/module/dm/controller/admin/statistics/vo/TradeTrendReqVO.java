package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author: Zeno
 * @createTime: 2024/07/03 17:10
 */
@Data
public class TradeTrendReqVO {

    @Schema(description = "门店ID")
    private String[] clientIds;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @Schema(description = "时间范围")
    private String[] times;
}
