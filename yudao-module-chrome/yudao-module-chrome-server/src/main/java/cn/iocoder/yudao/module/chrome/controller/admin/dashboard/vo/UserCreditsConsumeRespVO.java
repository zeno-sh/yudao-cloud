package cn.iocoder.yudao.module.chrome.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户积分消耗明细响应VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 用户积分消耗明细 Response VO")
@Data
public class UserCreditsConsumeRespVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    private String email;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "积分消耗总量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer creditsConsumed;

    @Schema(description = "使用次数", example = "10")
    private Integer usageCount;
}
