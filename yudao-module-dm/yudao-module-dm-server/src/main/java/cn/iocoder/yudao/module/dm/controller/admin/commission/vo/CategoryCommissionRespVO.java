package cn.iocoder.yudao.module.dm.controller.admin.commission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 类目佣金 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CategoryCommissionRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4549")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "类目名称", example = "芋艿")
    @ExcelProperty("类目名称")
    private String categoryName;

    @Schema(description = "类目佣金")
    @ExcelProperty("类目佣金")
    private BigDecimal rate;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "平台", example = "6892")
    @ExcelProperty("平台")
    private Long parentId;

}