package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

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
}
