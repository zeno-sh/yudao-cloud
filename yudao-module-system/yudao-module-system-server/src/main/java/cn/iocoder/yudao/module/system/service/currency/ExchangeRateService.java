package cn.iocoder.yudao.module.system.service.currency;

import cn.iocoder.yudao.module.system.dal.dataobject.currency.ExchangeRateDO;

import java.util.List;

/**
 * 汇率信息 Service 接口
 *
 * @author Jax
 */
public interface ExchangeRateService {

    /**
     * 获得汇率信息
     *
     * @param id 编号
     * @return 汇率信息
     */
    ExchangeRateDO getExchangeRate(Integer id);

    /**
     * 根据基础货币代码获得汇率信息
     *
     * @param baseCurrency 基础货币代码
     * @return 汇率信息
     */
    ExchangeRateDO getExchangeRateByBaseCurrency(String baseCurrency);

    /**
     * 获得汇率信息列表
     *
     * @return 汇率信息列表
     */
    List<ExchangeRateDO> getExchangeRateList();

}