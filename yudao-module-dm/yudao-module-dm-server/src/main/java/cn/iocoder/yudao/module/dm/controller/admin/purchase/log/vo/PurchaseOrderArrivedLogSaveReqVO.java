package cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 采购单到货日志新增/修改 Request VO")
@Data
public class PurchaseOrderArrivedLogSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4220")
    private Long id;

    @Schema(description = "采购单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3160")
    @NotNull(message = "采购单ID不能为空")
    private Long purchaseOrderId;

    @Schema(description = "采购单item ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10027")
    @NotNull(message = "采购单item ID不能为空")
    private Long purchaseOrderItemId;

    @Schema(description = "到货数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "到货数量不能为空")
    private Integer arrivedQuantity;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}