package cn.iocoder.yudao.module.dm.controller.admin.logistics.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 收费项目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FbsFeeServicesPageReqVO extends PageParam {

    @Schema(description = "海外仓ID", example = "25540")
    private Long warehouseId;

    @Schema(description = "海外仓名称", example = "25540")
    private String warehouseName;

    @Schema(description = "项目名称", example = "王五")
    private String name;

}