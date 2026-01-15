package cn.iocoder.yudao.module.dm.service.exchangerates.client;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 外部汇率 API 客户端实现
 * 
 * 调用 Private Currency API: http://1.117.17.136:9800/v1/latest
 * 返回的是 CNY -> Foreign (如 1 CNY = 0.14 USD)
 * 需转换为直接标价法: 1 外币 = XX CNY，公式: 1 / API_Rate
 *
 * @author Zeno
 */
@Component
@Slf4j
public class ExchangeRateApiClientImpl implements ExchangeRateApiClient {

    private static final String API_URL = "http://1.117.17.136:9800/v1/latest";

    @Override
    public Map<String, BigDecimal> getLatestRates(List<String> currencyCodes) {
        if (CollUtil.isEmpty(currencyCodes)) {
            return Collections.emptyMap();
        }

        try {
            // 构建请求 URL
            String symbols = String.join(",", currencyCodes);
            String url = API_URL + "?base=CNY&symbols=" + symbols;
            log.info("[getLatestRates] 调用外部汇率 API: {}", url);

            // 调用 API (使用 Hutool HttpUtil)
            String responseBody = cn.hutool.http.HttpUtil.get(url);
            if (responseBody == null) {
                log.warn("[getLatestRates] API 返回为空");
                return Collections.emptyMap();
            }

            // 解析响应 (使用 FastJSON)
            ExchangeRateApiResponse response = com.alibaba.fastjson2.JSON.parseObject(responseBody,
                    ExchangeRateApiResponse.class);
            if (response == null || response.getRates() == null) {
                log.warn("[getLatestRates] API 返回解析失败或 rates 为空");
                return Collections.emptyMap();
            }

            // 转换为直接标价法 (1 外币 = XX CNY)
            Map<String, BigDecimal> result = new HashMap<>();
            response.getRates().forEach((code, rate) -> {
                if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                    // 公式: 存储汇率 = 1 / API_Rate，保留 4 位小数
                    BigDecimal convertedRate = BigDecimal.ONE.divide(rate, 4, RoundingMode.HALF_UP);
                    result.put(code.toUpperCase(), convertedRate);
                    log.debug("[getLatestRates] {} API_Rate={} -> 存储汇率={}", code, rate, convertedRate);
                }
            });

            log.info("[getLatestRates] 获取到 {} 个币种的汇率", result.size());
            return result;

        } catch (Exception e) {
            log.error("[getLatestRates] 调用外部汇率 API 失败", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 外部 API 响应格式
     */
    @Data
    public static class ExchangeRateApiResponse {
        private String base;
        private String date;
        private BigDecimal amount;
        private Map<String, BigDecimal> rates;
    }

}
