package cn.iocoder.yudao.module.dm.rpc;

import cn.iocoder.yudao.module.dm.api.DmExchangeRateQueryService;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.module.dm.service.exchangerates.ExchangeRatesService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author: Zeno
 * @createTime: 2025/03/20 21:10
 */
@Service
public class DmExchangeRateQueryServiceImpl implements DmExchangeRateQueryService {

    @Resource
    private ExchangeRatesService exchangeRatesService;

    @Override
    public BigDecimal getExchangeRate(Integer baseCurrency) {
        ExchangeRatesDO exchangeRatesDO = exchangeRatesService.getExchangeRatesByBaseCurrency(baseCurrency);
        if (exchangeRatesDO != null) {
            return exchangeRatesDO.getCustomRate();
        }
        return null;
    }

    @Override
    public BigDecimal getExchangeRate(String currencyCode) {
        return null;
    }
}
