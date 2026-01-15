package cn.iocoder.yudao.module.chrome.controller.admin.collect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 商品采集请求 VO
 * 
 * @author Jax
 */
@Schema(description = "管理后台 - 商品采集 Request VO")
@Data
public class ProductCollectReqVO {

    @Schema(description = "商品URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.amazon.com/dp/B08N5WRWNW")
    @NotBlank(message = "商品URL不能为空")
    private String productUrl;

    @Schema(description = "平台类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "AMAZON")
    @NotBlank(message = "平台类型不能为空")
    private String platform;

    @Schema(description = "采集类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "BASIC")
    @NotBlank(message = "采集类型不能为空")
    private String collectType;

    @Schema(description = "是否包含评论", example = "true")
    private Boolean includeReviews = false;

    @Schema(description = "是否包含价格历史", example = "true")
    private Boolean includePriceHistory = false;

    @Schema(description = "最大采集数量", example = "100")
    private Integer maxCount = 50;

}