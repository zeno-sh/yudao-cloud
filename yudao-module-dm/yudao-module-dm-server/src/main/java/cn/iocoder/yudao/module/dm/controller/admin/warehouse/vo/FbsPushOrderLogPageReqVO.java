package cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 海外仓推单记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FbsPushOrderLogPageReqVO extends PageParam {

    @Schema(description = "关联的仓库ID", example = "4211")
    private Long warehouseId;

    @Schema(description = "平台订单ID", example = "8098")
    private String platformOrderId;

    @Schema(description = "请求参数")
    private String request;

    @Schema(description = "响应结果")
    private String response;

    @Schema(description = "状态", example = "1")
    private Boolean status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}