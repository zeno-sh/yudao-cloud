package cn.iocoder.yudao.module.system.service.currency;

import cn.iocoder.yudao.module.system.dal.dataobject.currency.CurrencyDO;
import cn.iocoder.yudao.module.system.dal.mysql.currency.CurrencyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 币种信息 Service 实现类
 *
 * @author Jax
 */
@Service
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

    @Resource
    private CurrencyMapper currencyMapper;

    @Override
    public CurrencyDO getCurrency(Integer id) {
        return currencyMapper.selectById(id);
    }

    @Override
    public CurrencyDO getCurrencyByCode(String currencyCode) {
        return currencyMapper.selectByCurrencyCode(currencyCode);
    }

    @Override
    public List<CurrencyDO> getCurrencyList() {
        return currencyMapper.selectList();
    }

}