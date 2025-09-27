package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo;

import lombok.*;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 财务账单报告分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProfitReportPageReqVO extends PageParam {

    @Schema(description = "账单日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] financeDate;

    @Schema(description = "门店ID", example = "7216")
    private String[] clientIds;

    @Schema(description = "平台货号", example = "28710")
    private String offerId;

    @Schema(description = "查询维度", example = "client、sku")
    private String dimension;

    @Schema(description = "分组类型", example = "day、month、week")
    private String groupType;
}