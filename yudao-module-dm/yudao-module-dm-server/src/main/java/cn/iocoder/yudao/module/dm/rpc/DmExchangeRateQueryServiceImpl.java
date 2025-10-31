package cn.iocoder.yudao.module.dm.rpc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.api.DmExchangeRateQueryService;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.module.dm.service.exchangerates.ExchangeRatesService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author: Zeno
 * @createTime: 2025/03/20 21:10
 */
@RestController
public class DmExchangeRateQueryServiceImpl implements DmExchangeRateQueryService {

    @Resource
    private ExchangeRatesService exchangeRatesService;

    @Override
    public CommonResult<BigDecimal> getExchangeRate(Integer baseCurrency) {
        ExchangeRatesDO exchangeRatesDO = exchangeRatesService.getExchangeRatesByBaseCurrency(baseCurrency);
        if (exchangeRatesDO != null) {
            return CommonResult.success(exchangeRatesDO.getCustomRate());
        }
        return null;
    }

}
