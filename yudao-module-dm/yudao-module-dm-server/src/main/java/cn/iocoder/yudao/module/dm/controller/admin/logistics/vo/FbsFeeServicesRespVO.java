package cn.iocoder.yudao.module.dm.controller.admin.logistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 收费项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FbsFeeServicesRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23711")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "海外仓ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25540")
    @ExcelProperty("海外仓ID")
    private Long warehouseId;

    @Schema(description = "海外仓", requiredMode = Schema.RequiredMode.REQUIRED, example = "25540")
    @ExcelProperty("海外仓名称")
    private String warehouseName;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("项目名称")
    private String name;

    @Schema(description = "项目标签")
    private Integer tag;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}