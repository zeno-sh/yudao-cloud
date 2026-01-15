package cn.iocoder.yudao.module.chrome.controller.admin.usage.vo;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - Chrome使用记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UsageRecordPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "25311")
    private Long userId;

    @Schema(description = "功能类型（10商品采集 20排名采集 30评论采集 40销量采集 50趋势采集 60类目分析）", example = "2")
    private Integer featureType;

    @Schema(description = "使用日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] usageDate;

    @Schema(description = "使用次数", example = "9404")
    private Integer usageCount;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "商品ID", example = "1")
    private String sellerProductId;
}