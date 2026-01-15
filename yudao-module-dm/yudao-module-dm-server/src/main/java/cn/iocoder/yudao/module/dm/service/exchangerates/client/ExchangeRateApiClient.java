package cn.iocoder.yudao.module.dm.service.exchangerates.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 外部汇率 API 客户端接口
 * 
 * 用于调用外部汇率 API 获取实时汇率
 * API URL: http://1.117.17.136:9800/v1/latest?base=cny&symbols=USD,KRW,JPY
 *
 * @author Zeno
 */
public interface ExchangeRateApiClient {

    /**
     * 获取最新汇率
     *
     * @param currencyCodes 币种代码列表（如 USD, KRW, JPY）
     * @return Map<币种代码, 汇率值>（已转换为直接标价法：1外币=XX CNY）
     */
    Map<String, BigDecimal> getLatestRates(List<String> currencyCodes);

}
