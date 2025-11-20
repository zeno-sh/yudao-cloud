package cn.iocoder.yudao.module.system.service.currency;

import cn.iocoder.yudao.module.system.dal.dataobject.currency.ExchangeRateDO;
import cn.iocoder.yudao.module.system.dal.mysql.currency.ExchangeRateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 汇率信息 Service 实现类
 *
 * @author Jax
 */
@Service
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {

    @Resource
    private ExchangeRateMapper exchangeRateMapper;

    @Override
    public ExchangeRateDO getExchangeRate(Integer id) {
        return exchangeRateMapper.selectById(id);
    }

    @Override
    public ExchangeRateDO getExchangeRateByBaseCurrency(String baseCurrency) {
        return exchangeRateMapper.selectByBaseCurrency(baseCurrency);
    }

    @Override
    public List<ExchangeRateDO> getExchangeRateList() {
        return exchangeRateMapper.selectList();
    }

}