package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.dto.ProductCostsDTO;
import cn.iocoder.yudao.module.dm.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

/**
 * 产品成本
 * @author Jax
 */
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 产品成本")
public interface DmProductCostsQueryService {

    String PREFIX = ApiConstants.PREFIX + "/product-costs";

    @GetMapping(PREFIX + "/getByProductId")
    @Operation(summary = "根据产品ID获取产品成本信息")
    @Parameter(name = "productId", description = "产品ID", required = true)
    CommonResult<ProductCostsDTO> getProductCostsByProductId(@RequestParam("productId") Long productId);

    @GetMapping(PREFIX + "/batchGetByProductIds")
    @Operation(summary = "根据产品ID列表批量获取产品成本信息")
    @Parameter(name = "productIds", description = "产品ID列表，逗号分隔", required = true)
    CommonResult<List<ProductCostsDTO>> batchGetProductCostsByProductIds(@RequestParam("productIds") Collection<Long> productIds);

    @GetMapping(PREFIX + "/getByPlatformAndProductId")
    @Operation(summary = "根据平台和产品ID获取产品成本信息")
    @Parameter(name = "platform", description = "目标平台", required = true)
    @Parameter(name = "productId", description = "产品ID", required = true)
    CommonResult<ProductCostsDTO> getProductCostsByPlatformAndProductId(@RequestParam("platform") Integer platform,
                                                                        @RequestParam("productId") Long productId);

    @GetMapping(PREFIX + "/batchGetByPlatformAndProductIds")
    @Operation(summary = "根据平台和产品ID列表批量获取产品成本信息")
    @Parameter(name = "platform", description = "目标平台", required = true)
    @Parameter(name = "productIds", description = "产品ID列表，逗号分隔", required = true)
    CommonResult<List<ProductCostsDTO>> batchGetProductCostsByPlatformAndProductIds(@RequestParam("platform") Integer platform,
                                                                                    @RequestParam("productIds") Collection<Long> productIds);

} 