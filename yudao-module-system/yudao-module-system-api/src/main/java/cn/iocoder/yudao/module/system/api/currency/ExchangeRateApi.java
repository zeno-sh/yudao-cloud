package cn.iocoder.yudao.module.system.api.currency;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.api.currency.dto.ExchangeRateRespDTO;
import cn.iocoder.yudao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 汇率信息 API 接口
 *
 * @author Jax
 */
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 汇率信息")
public interface ExchangeRateApi {

    String PREFIX = ApiConstants.PREFIX + "/exchange";

    /**
     * 获得汇率信息
     *
     * @param id 汇率编号
     * @return 汇率信息
     */
    @GetMapping(PREFIX + "/get")
    @Operation(summary = "获得汇率信息")
    @Parameter(name = "id", description = "汇率编号", required = true, example = "1")
    CommonResult<ExchangeRateRespDTO> getExchangeRate(@RequestParam("id") Integer id);

    /**
     * 根据基础货币代码获得汇率信息
     *
     * @param baseCurrency 基础货币代码
     * @return 汇率信息
     */
    @GetMapping(PREFIX + "/get-by-base-currency")
    @Operation(summary = "根据基础货币代码获得汇率信息")
    @Parameter(name = "baseCurrency", description = "基础货币代码", required = true, example = "USD")
    CommonResult<ExchangeRateRespDTO> getExchangeRateByBaseCurrency(@RequestParam("baseCurrency") String baseCurrency);

    /**
     * 获得汇率信息列表
     *
     * @return 汇率信息列表
     */
    @GetMapping(PREFIX + "/list")
    @Operation(summary = "获得汇率信息列表")
    CommonResult<List<ExchangeRateRespDTO>> getExchangeRateList();


}