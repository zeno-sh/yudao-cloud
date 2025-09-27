package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 广告活动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OzonAdCampaignsPageReqVO extends PageParam {

    @Schema(description = "门店id", example = "24908")
    private String[] clientIds;

    @Schema(description = "活动ID", example = "4851")
    private String campaignId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "展示量")
    private Integer views;

    @Schema(description = "点击数")
    private Integer clicks;

    @Schema(description = "广告花费")
    private BigDecimal moneySpent;

    @Schema(description = "平均报价", example = "29986")
    private BigDecimal avgBid;

    @Schema(description = "订单数量")
    private Integer orders;

    @Schema(description = "订单金额")
    private BigDecimal ordersMoney;

    @Schema(description = "日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] date;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "分组类型", example = "day、month、week")
    private String groupType;

}