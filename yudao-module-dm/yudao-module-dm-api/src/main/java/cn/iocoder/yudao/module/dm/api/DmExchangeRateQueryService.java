package cn.iocoder.yudao.module.dm.api;

import java.math.BigDecimal;

/**
 * 汇率
 * @author: Zeno
 * @createTime: 2025/03/20 21:08
 */
public interface DmExchangeRateQueryService {

    /**
     * 获取自定义汇率
     * @param baseCurrency 基础币种
     * @return 汇率
     */
    BigDecimal getExchangeRate(Integer baseCurrency);

    /**
     * 获取自定义汇率
     * @param currencyCode 币种
     * @return 汇率
     */
    BigDecimal getExchangeRate(String currencyCode);
}
