package cn.iocoder.yudao.module.chrome.controller.plugin.trends.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 关键词趋势查询请求VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 关键词趋势查询请求")
@Data
public class KeywordTrendsQueryReqVO {

    @Schema(description = "查询关键词", requiredMode = Schema.RequiredMode.REQUIRED, example = "iPad")
    @NotBlank(message = "查询关键词不能为空")
    private String query;

    @Schema(description = "起始位置", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0")
    private Integer start = 0;

    @Schema(description = "查询数量限制", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "100")
    private Integer limit = 100;

    @Schema(description = "Cookie字符串", requiredMode = Schema.RequiredMode.NOT_REQUIRED, 
            example = "sxSessionId=OWYzZjRhMjItOTAyOC00YzU3LWFmOWUtZjVlMWQ5NWVmZDkx;")
    private String cookie;
}
