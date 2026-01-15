package cn.iocoder.yudao.module.system.service.currency;

import cn.iocoder.yudao.module.system.dal.dataobject.currency.CurrencyDO;

import java.util.List;

/**
 * 币种信息 Service 接口
 *
 * @author Jax
 */
public interface CurrencyService {

    /**
     * 获得币种信息
     *
     * @param id 编号
     * @return 币种信息
     */
    CurrencyDO getCurrency(Integer id);

    /**
     * 根据货币代码获得币种信息
     *
     * @param currencyCode 货币代码
     * @return 币种信息
     */
    CurrencyDO getCurrencyByCode(String currencyCode);

    /**
     * 获得币种信息列表
     *
     * @return 币种信息列表
     */
    List<CurrencyDO> getCurrencyList();

    /**
     * 获得启用状态的币种列表
     *
     * @return 启用状态的币种信息列表
     */
    List<CurrencyDO> getEnabledCurrencyList();

}