package cn.iocoder.yudao.module.dm.rpc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.api.DmExchangeRateService;
import cn.iocoder.yudao.module.dm.api.dto.InitExchangeRatesReqDTO;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.module.dm.service.exchangerates.ExchangeRatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 汇率 RPC 接口实现类
 *
 * @author: Zeno
 * @createTime: 2025/03/20 21:10
 */
@RestController
@Slf4j
public class DmExchangeRateServiceImpl implements DmExchangeRateService {

    @Resource
    private ExchangeRatesService exchangeRatesService;

    @Override
    public CommonResult<BigDecimal> getExchangeRate(String currency) {
        ExchangeRatesDO exchangeRatesDO = exchangeRatesService.getExchangeRatesByCurrencyCode(currency);
        if (exchangeRatesDO != null) {
            return CommonResult.success(exchangeRatesDO.getCustomRate());
        }
        return CommonResult.success(null);
    }

    @Override
    public CommonResult<Boolean> initExchangeRates(InitExchangeRatesReqDTO reqDTO) {
        log.info("[initExchangeRates] 收到汇率初始化请求: 租户ID={}, 币种数量={}",
                reqDTO.getTenantId(),
                CollUtil.isEmpty(reqDTO.getCurrencyCodes()) ? 0 : reqDTO.getCurrencyCodes().size());
        try {
            exchangeRatesService.batchInitExchangeRates(reqDTO.getTenantId(), reqDTO.getCurrencyCodes());
            return CommonResult.success(true);
        } catch (Exception e) {
            log.error("[initExchangeRates] 汇率初始化失败: 租户ID={}", reqDTO.getTenantId(), e);
            return CommonResult.success(false);
        }
    }

}
