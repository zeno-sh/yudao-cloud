package cn.iocoder.yudao.module.dm.controller.admin.brand.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;


@Schema(description = "管理后台 - 品牌信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DmProductBrandRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1731")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "品牌名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("品牌名称")
    private String name;

    @Schema(description = "Logo")
    @ExcelProperty("Logo")
    private String logo;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}