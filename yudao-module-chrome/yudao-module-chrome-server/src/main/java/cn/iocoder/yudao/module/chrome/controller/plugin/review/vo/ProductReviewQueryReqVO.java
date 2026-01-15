package cn.iocoder.yudao.module.chrome.controller.plugin.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 产品评论查询请求VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 产品评论查询请求")
@Data
public class ProductReviewQueryReqVO {

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1747842988")
    @NotNull(message = "产品ID不能为空")
    @Positive(message = "产品ID必须为正数")
    private Long productId;

    @Schema(description = "开始时间戳(毫秒)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1640995200000")
    private Long startTime;

    @Schema(description = "结束时间戳(毫秒)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1672531199000")
    private Long endTime;

    @Schema(description = "Cookie字符串", requiredMode = Schema.RequiredMode.NOT_REQUIRED, 
            example = "x-coupang-target-market=KR; x-coupang-accept-language=ko-KR; PCID=17549616453407215840903")
    private String cookie;

    @Schema(description = "查询模式", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "ALL")
    private String queryMode = "ALL";

    @Schema(description = "页码", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "20")
    private Integer size = 10;

    @Schema(description = "排序方式", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "DATE_DESC")
    private String sortBy = "DATE_DESC";

    @Schema(description = "最大页数限制", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Integer maxPages;
}
