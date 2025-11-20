package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 汇率
 * @author: Zeno
 * @createTime: 2025/03/20 21:08
 */
@FeignClient(name = ApiConstants.NAME) // ① @FeignClient 注解
@Tag(name = "RPC 服务 - 汇率") // ② Swagger 接口文档
public interface DmExchangeRateQueryService {

    String PREFIX = ApiConstants.PREFIX + "/exchange";

    @GetMapping(PREFIX + "/getExchangeRate") // ③ Spring MVC 接口注解
    @Operation(summary = "获取自定义汇率")  // ② Swagger 接口文档
    @Parameter(name = "baseCurrency", description = "币种", required = true, example = "USD") // ② Swagger 接口文档
    CommonResult<BigDecimal> getExchangeRate(@RequestParam("baseCurrency") String baseCurrency);

}
