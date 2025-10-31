package cn.iocoder.yudao.module.dm.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - Ozon订单 Request VO")
@Data
@ToString(callSuper = true)
public class OzonOrderSyncReqVO {

    @Schema(description = "平台门店id", example = "22208")
    private String[] clientIds;

    @Schema(description = "接单时间")
    @NotNull(message = "接单时间不能为空")
    private String[] inProcessAt;

}