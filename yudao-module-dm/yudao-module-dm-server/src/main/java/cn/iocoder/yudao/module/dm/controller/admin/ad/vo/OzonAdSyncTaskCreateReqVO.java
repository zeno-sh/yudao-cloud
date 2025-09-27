package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

/**
 * Ozon广告同步任务创建 Request VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - Ozon广告同步任务创建 Request VO")
@Data
public class OzonAdSyncTaskCreateReqVO {

    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @Schema(description = "客户端ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate beginDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate endDate;

    @Schema(description = "备注", example = "手动创建的同步任务")
    private String remark;

}