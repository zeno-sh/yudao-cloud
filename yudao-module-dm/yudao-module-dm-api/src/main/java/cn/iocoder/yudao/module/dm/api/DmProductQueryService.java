package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.dto.ProductSimpleInfoDTO;
import cn.iocoder.yudao.module.dm.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Jax
 * @createTime: 2025/08/11 17:12
 */
@FeignClient(name = ApiConstants.NAME) // ① @FeignClient 注解
@Tag(name = "RPC 服务 - ERP产品管理") // ② Swagger 接口文档
public interface DmProductQueryService {

    String PREFIX = ApiConstants.PREFIX + "/product";

    /**
     * 获取用户产品权限的 ID 集合
     *
     * @return
     */
    @GetMapping(PREFIX + "/getProductIdsByUserPermission") // ③ Spring MVC 接口注解
    @Operation(summary = "获取用户产品权限的 ID 集合")  // ② Swagger 接口文档
    CommonResult<Set<Long>> getProductIdsByUserPermission();

    /**
     * 批量查询产品简单信息
     *
     * @param productIds 产品ID列表
     * @return 产品ID到产品简单信息的映射
     */
    @GetMapping(PREFIX + "/batchQueryProductSimpleInfo")
    @Operation(summary = "批量查询产品简单信息")
    @Parameter(name = "productIds", description = "产品ID列表", required = true)
    CommonResult<Map<Long, ProductSimpleInfoDTO>> batchQueryProductSimpleInfo(@RequestParam("productIds") List<Long> productIds);
}
