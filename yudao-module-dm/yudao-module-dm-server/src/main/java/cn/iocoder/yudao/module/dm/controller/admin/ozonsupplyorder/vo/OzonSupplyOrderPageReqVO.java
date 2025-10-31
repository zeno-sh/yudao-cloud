package cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "供应订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OzonSupplyOrderPageReqVO extends PageParam {

    @Schema(description = "客户端ID数组")
    private String[] clientIds;

    @Schema(description = "供应订单ID", example = "12345")
    private Long supplyOrderId;

    @Schema(description = "状态", example = "awaiting_packaging")
    private String state;

    @Schema(description = "仓库ID", example = "12345")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "北京仓")
    private String warehouseName;

    @Schema(description = "时间槽开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime timeslotFrom;

    @Schema(description = "时间槽结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime timeslotTo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}