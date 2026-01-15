package cn.iocoder.yudao.module.system.api.currency;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.api.currency.dto.CurrencyRespDTO;
import cn.iocoder.yudao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 币种信息 API 接口
 *
 * @author Jax
 */
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 币种信息")
public interface CurrencyApi {

    String PREFIX = ApiConstants.PREFIX + "/currency";

    /**
     * 获得币种信息
     *
     * @param id 币种编号
     * @return 币种信息
     */
    @GetMapping(PREFIX + "/get")
    @Operation(summary = "获得币种信息")
    @Parameter(name = "id", description = "币种编号", required = true, example = "1")
    CommonResult<CurrencyRespDTO> getCurrency(@RequestParam("id") Integer id);

    /**
     * 根据货币代码获得币种信息
     *
     * @param currencyCode 货币代码
     * @return 币种信息
     */
    @GetMapping(PREFIX + "/get-by-code")
    @Operation(summary = "根据货币代码获得币种信息")
    @Parameter(name = "currencyCode", description = "货币代码", required = true, example = "CNY")
    CommonResult<CurrencyRespDTO> getCurrencyByCode(@RequestParam("currencyCode") String currencyCode);

    /**
     * 获得币种信息列表
     *
     * @return 币种信息列表
     */
    @GetMapping(PREFIX + "/list")
    @Operation(summary = "获得币种信息列表")
    CommonResult<List<CurrencyRespDTO>> getCurrencyList();

    /**
     * 获得启用状态的币种列表
     *
     * @return 启用状态的币种信息列表
     */
    @GetMapping(PREFIX + "/list-enabled")
    @Operation(summary = "获得启用状态的币种列表")
    CommonResult<List<CurrencyRespDTO>> getEnabledCurrencyList();

}