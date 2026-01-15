package cn.iocoder.yudao.module.chrome.controller.plugin.sales.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 产品销量查询请求VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 产品销量查询请求")
@Data
public class ProductSalesQueryReqVO {

    @Schema(description = "关键词(sellerProductId)", requiredMode = Schema.RequiredMode.REQUIRED, example = "8820001925")
    @NotBlank(message = "关键词不能为空")
    private String keyword;

    @Schema(description = "Cookie字符串", requiredMode = Schema.RequiredMode.NOT_REQUIRED, 
            example = "sxSessionId=OWYzZjRhMjItOTAyOC00YzU3LWFmOWUtZjVlMWQ5NWVmZDkx;")
    private String cookie;
}
