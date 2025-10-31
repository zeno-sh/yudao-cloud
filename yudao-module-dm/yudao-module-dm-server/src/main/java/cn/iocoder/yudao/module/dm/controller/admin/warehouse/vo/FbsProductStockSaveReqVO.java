package cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - 海外仓产品库存新增/修改 Request VO")
@Data
public class FbsProductStockSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2654")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29752")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "仓库Sku", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "仓库Sku不能为空")
    private String productSku;

    @Schema(description = "本地产品ID", example = "11463")
    private Long productId;

    @Schema(description = "在途数量")
    private Integer onway;

    @Schema(description = "可售数量")
    private Integer sellable;

    @Schema(description = "历史出库数量")
    private Integer shipped;

}