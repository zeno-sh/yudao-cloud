package cn.iocoder.yudao.module.dm.controller.admin.commission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 类目佣金新增/修改 Request VO")
@Data
public class CategoryCommissionSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4549")
    private Long id;

    @Schema(description = "类目名称", example = "芋艿")
    private String categoryName;

    @Schema(description = "类目佣金")
    private BigDecimal rate;

    @Schema(description = "平台", example = "6892")
    private Long parentId;

}