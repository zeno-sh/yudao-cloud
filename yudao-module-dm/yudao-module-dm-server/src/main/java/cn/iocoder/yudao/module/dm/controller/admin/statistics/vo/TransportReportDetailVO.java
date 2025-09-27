package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 发货计划日维度明细 Response VO")
@Data
public class TransportReportDetailVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long productId;

    @Schema(description = "日期，格式为：yyyy-MM-dd", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-12-01")
    private String date;

    @Schema(description = "出货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer outboundNum;

    @Schema(description = "进仓数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer inboundNum;
} 