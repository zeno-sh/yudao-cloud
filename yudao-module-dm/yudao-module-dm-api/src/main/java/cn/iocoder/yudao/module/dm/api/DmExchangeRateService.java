package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.api.dto.InitExchangeRatesReqDTO;
import cn.iocoder.yudao.module.dm.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 汇率 RPC 接口
 * 
 * @author: Zeno
 * @createTime: 2025/03/20 21:08
 */
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 汇率")
public interface DmExchangeRateService {

    String PREFIX = ApiConstants.PREFIX + "/exchange";

    @GetMapping(PREFIX + "/getExchangeRate")
    @Operation(summary = "获取自定义汇率")
    @Parameter(name = "baseCurrency", description = "币种", required = true, example = "USD")
    CommonResult<BigDecimal> getExchangeRate(@RequestParam("currency") String currency);

    /**
     * 批量初始化租户汇率
     *
     * @param reqDTO 初始化请求（包含租户ID和币种列表）
     * @return 是否成功
     */
    @PostMapping(PREFIX + "/batch-init")
    @Operation(summary = "批量初始化租户汇率")
    CommonResult<Boolean> initExchangeRates(@RequestBody InitExchangeRatesReqDTO reqDTO);

}
