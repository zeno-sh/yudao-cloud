package cn.iocoder.yudao.module.dm.service.exchangerates;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.exchangerates.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 汇率 Service 接口
 *
 * @author Zeno
 */
public interface ExchangeRatesService {

    /**
     * 创建汇率
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createExchangeRates(@Valid ExchangeRatesSaveReqVO createReqVO);

    /**
     * 更新汇率
     *
     * @param updateReqVO 更新信息
     */
    void updateExchangeRates(@Valid ExchangeRatesSaveReqVO updateReqVO);

    /**
     * 删除汇率
     *
     * @param id 编号
     */
    void deleteExchangeRates(Long id);

    /**
     * 获得汇率
     *
     * @param id 编号
     * @return 汇率
     */
    ExchangeRatesDO getExchangeRates(Long id);

    /**
     * 获得汇率分页
     *
     * @param pageReqVO 分页查询
     * @return 汇率分页
     */
    PageResult<ExchangeRatesDO> getExchangeRatesPage(ExchangeRatesPageReqVO pageReqVO);

    /**
     * 根据基础货币
     *
     * @param baseCurrency
     * @return
     */
    ExchangeRatesDO getExchangeRatesByBaseCurrency(Integer baseCurrency);

    /**
     * 根据货币代码
     *
     * @param currencyCode
     * @return
     */
    ExchangeRatesDO getExchangeRatesByCurrencyCode(String currencyCode);
}