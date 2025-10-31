package cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 海外仓推单记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FbsPushOrderLogRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "28203")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "关联的仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4211")
    @ExcelProperty("关联的仓库ID")
    private Long warehouseId;

    @Schema(description = "本地订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8098")
    @ExcelProperty("本地订单ID")
    private Long dmOrderId;

    @Schema(description = "请求参数")
    @ExcelProperty("请求参数")
    private String request;

    @Schema(description = "响应结果")
    @ExcelProperty("响应结果")
    private String response;

    @Schema(description = "状态", example = "1")
    @ExcelProperty("状态")
    private Boolean status;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}