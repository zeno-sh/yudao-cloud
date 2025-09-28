package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.dto.ProductPurchaseDTO;
import cn.iocoder.yudao.module.dm.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

/**
 * 产品采购
 * @author Jax
 */
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 产品采购")
public interface DmProductPurchaseQueryService {

    String PREFIX = ApiConstants.PREFIX + "/product-purchase";

    @GetMapping(PREFIX + "/getByProductId")
    @Operation(summary = "根据产品ID获取产品采购信息")
    @Parameter(name = "productId", description = "产品ID", required = true)
    CommonResult<ProductPurchaseDTO> getProductPurchaseByProductId(@RequestParam("productId") Long productId);

    @GetMapping(PREFIX + "/batchGetByProductIds")
    @Operation(summary = "根据产品ID列表批量获取产品采购信息")
    @Parameter(name = "productIds", description = "产品ID列表，逗号分隔", required = true)
    CommonResult<Map<Long, ProductPurchaseDTO>> batchGetProductPurchaseByProductIds(@RequestParam("productIds") Collection<Long> productIds);
}