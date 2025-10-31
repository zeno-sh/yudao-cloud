package cn.iocoder.yudao.module.dm.controller.admin.order.vo;

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

@Schema(description = "管理后台 - Ozon订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OzonOrderPageReqVO extends PageParam {

    @Schema(description = "平台门店id", example = "22208")
    private String[] clientIds;

    @Schema(description = "平台订单id", example = "287")
    private String orderId;

    @Schema(description = "订单类型", example = "287")
    private Integer orderType;

    @Schema(description = "发货编号")
    private String postingNumber;

    @Schema(description = "货号")
    private String offerId;

    @Schema(description = "订单状态", example = "1")
    private String status;

    @Schema(description = "接单时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private String[] inProcessAt;

    @Schema(description = "发运时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] shipmentDate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private String todayShipmentDate;

    private LocalDateTime[] inProcessAtParams;
    private LocalDate[] shipmentDateParams;
}