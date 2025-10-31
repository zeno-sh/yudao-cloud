package cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 海外仓推单记录新增/修改 Request VO")
@Data
public class FbsPushOrderLogSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "28203")
    private Long id;

    @Schema(description = "关联的仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4211")
    @NotNull(message = "关联的仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "平台订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8098")
    @NotNull(message = "平台订单ID不能为空")
    private String platformOrderId;

    @Schema(description = "发货编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8098")
    @NotNull(message = "平台发货编号")
    private String postingNumber;

    @Schema(description = "关联的订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "关联的订单ID不能为空")
    private Long orderId;

    @Schema(description = "请求参数")
    private String request;

    @Schema(description = "响应结果")
    private String response;

    @Schema(description = "状态", example = "1")
    private Boolean status;

}