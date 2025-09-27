package cn.iocoder.yudao.module.dm.controller.admin.transport.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 头程计划分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TransportPlanPageReqVO extends PageParam {

    @Schema(description = "发货计划")
    private String code;

    @Schema(description = "运输状态", example = "2")
    private Integer transportStatus;

    @Schema(description = "海外仓入库单号", example = "7794")
    private String overseaLocationCheckinId;

    @Schema(description = "货代公司")
    private String forwarder;

    @Schema(description = "报价", example = "25376")
    private BigDecimal offerPrice;

    @Schema(description = "币种")
    private Integer currency;

    @Schema(description = "结算状态", example = "2")
    private String settleStatus;

    @Schema(description = "账单（人民币）", example = "8462")
    private BigDecimal billPrice;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "发运日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] despatchDate;

    @Schema(description = "预计抵达日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] arrivalDate;

    @Schema(description = "产品ID")
    private Long productId;

}