package cn.iocoder.yudao.module.dm.service.exchangerates;

import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.dm.controller.admin.exchangerates.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.exchangerates.ExchangeRatesMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 汇率 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class ExchangeRatesServiceImpl implements ExchangeRatesService {

    @Resource
    private ExchangeRatesMapper exchangeRatesMapper;

    @Override
    public Long createExchangeRates(ExchangeRatesSaveReqVO createReqVO) {
        // 插入
        ExchangeRatesDO exchangeRates = BeanUtils.toBean(createReqVO, ExchangeRatesDO.class);
        exchangeRatesMapper.insert(exchangeRates);
        // 返回
        return exchangeRates.getId();
    }

    @Override
    public void updateExchangeRates(ExchangeRatesSaveReqVO updateReqVO) {
        // 校验存在
        validateExchangeRatesExists(updateReqVO.getId());
        // 更新
        ExchangeRatesDO updateObj = BeanUtils.toBean(updateReqVO, ExchangeRatesDO.class);
        exchangeRatesMapper.updateById(updateObj);
    }

    @Override
    public void deleteExchangeRates(Long id) {
        // 校验存在
        validateExchangeRatesExists(id);
        // 删除
        exchangeRatesMapper.deleteById(id);
    }

    private void validateExchangeRatesExists(Long id) {
        if (exchangeRatesMapper.selectById(id) == null) {
            throw exception(EXCHANGE_RATES_NOT_EXISTS);
        }
    }

    @Override
    public ExchangeRatesDO getExchangeRates(Long id) {
        return exchangeRatesMapper.selectById(id);
    }

    @Override
    public PageResult<ExchangeRatesDO> getExchangeRatesPage(ExchangeRatesPageReqVO pageReqVO) {
        return exchangeRatesMapper.selectPage(pageReqVO);
    }

    @Override
    public ExchangeRatesDO getExchangeRatesByCurrencyCode(String currencyCode) {
        LambdaQueryWrapperX<ExchangeRatesDO> queryWrapperX = new LambdaQueryWrapperX<ExchangeRatesDO>()
                .eqIfPresent(ExchangeRatesDO::getCurrencyCode, currencyCode);
        return exchangeRatesMapper.selectOne(queryWrapperX);
    }
}