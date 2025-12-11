package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.dto.ShopMappingDTO;
import cn.iocoder.yudao.module.dm.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 门店映射
 * 
 * @author Jax
 * @createTime: 2025/01/16 10:00
 */
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 门店映射")
public interface DmShopMappingQueryService {

    String PREFIX = ApiConstants.PREFIX + "/shop-mapping";

    @GetMapping(PREFIX + "/getByClientId")
    @Operation(summary = "根据客户端ID查询绑定门店信息")
    @Parameter(name = "clientId", description = "客户端ID", required = true)
    CommonResult<ShopMappingDTO> getShopMappingByClientId(@RequestParam("clientId") String clientId);

    @GetMapping(PREFIX + "/batchGetByClientIds")
    @Operation(summary = "根据客户端ID列表批量查询绑定门店信息")
    @Parameter(name = "clientIds", description = "客户端ID列表，逗号分隔", required = true)
    CommonResult<List<ShopMappingDTO>> batchGetShopMappingByClientIds(@RequestParam("clientIds") List<String> clientIds);

    @GetMapping(PREFIX + "/getAllAvailable")
    @Operation(summary = "查询所有可用的门店映射信息")
    CommonResult<List<ShopMappingDTO>> getAllAvailableShopMappings();

    @GetMapping(PREFIX + "/getAllByTenant")
    @Operation(summary = "查询当前租户所有门店映射信息")
    CommonResult<List<ShopMappingDTO>> getAllShopByTenant();
}