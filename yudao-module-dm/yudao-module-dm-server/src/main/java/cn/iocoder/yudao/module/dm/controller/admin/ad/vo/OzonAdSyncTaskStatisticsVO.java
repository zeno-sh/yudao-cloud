package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Ozon广告同步任务统计信息 VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - Ozon广告同步任务统计信息 VO")
@Data
public class OzonAdSyncTaskStatisticsVO {

    @Schema(description = "总任务数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long totalCount;

    @Schema(description = "待处理任务数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long pendingCount;

    @Schema(description = "处理中任务数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long processingCount;

    @Schema(description = "已完成任务数", requiredMode = Schema.RequiredMode.REQUIRED, example = "70")
    private Long completedCount;

    @Schema(description = "失败任务数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long failedCount;

}