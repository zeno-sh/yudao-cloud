package cn.iocoder.yudao.module.dm.controller.admin.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - 组合产品明细 Request VO")
@Data
public class ProductBundleItemReqVO {

    @Schema(description = "子产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "子产品ID不能为空")
    private Long subProductId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}

