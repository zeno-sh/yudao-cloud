package cn.iocoder.yudao.module.dm.controller.admin.brand.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 品牌信息新增/修改 Request VO")
@Data
public class DmProductBrandSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1731")
    private Long id;

    @Schema(description = "品牌名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "品牌名称不能为空")
    private String name;

    @Schema(description = "Logo")
    private String logo;

}