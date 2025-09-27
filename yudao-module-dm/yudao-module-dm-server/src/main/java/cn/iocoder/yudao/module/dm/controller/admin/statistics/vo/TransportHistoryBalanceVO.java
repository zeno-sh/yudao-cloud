package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 发货计划历史余额 Response VO")
@Data
public class TransportHistoryBalanceVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long productId;

    @Schema(description = "历史余额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer balance;
} 