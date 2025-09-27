package cn.iocoder.yudao.module.dm.service.profitreport.v2.data;

import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitCalculationRequestVO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.ExchangeRateData;
import cn.iocoder.yudao.module.dm.service.exchangerates.ExchangeRatesService;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 汇率数据收集器
 * 负责收集计算所需的汇率数据
 *
 * @author Jax
 */
@Component
@Slf4j
public class ExchangeRateDataCollector {
    
    @Resource
    private ExchangeRatesService exchangeRatesService;
    
    // 默认汇率（当无法获取实时汇率时使用）
    private static final BigDecimal DEFAULT_RUB_TO_CNY = new BigDecimal("0.075");
    private static final BigDecimal DEFAULT_USD_TO_CNY = new BigDecimal("7.018");
    
    /**
     * 收集汇率数据
     *
     * @param request 计算请求参数
     * @param taskId 任务ID
     * @return 汇率数据
     */
    public ExchangeRateData collect(ProfitCalculationRequestVO request, String taskId) {
        // 只取第一个clientId
        String clientId = (request.getClientIds() != null && request.getClientIds().length > 0) 
                ? request.getClientIds()[0] : null;
        
        log.info("开始收集汇率数据: clientId={}, dateRange={}, taskId={}", 
                clientId, request.getFinanceDate(), taskId);
        
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        if (request.getFinanceDate() != null && request.getFinanceDate().length >= 2) {
            startDate = LocalDate.parse(request.getFinanceDate()[0]);
            endDate = LocalDate.parse(request.getFinanceDate()[1]);
        }
        
        try {
            // 获取RUB到CNY的汇率（基于现有Service接口）
            ExchangeRatesDO rubToCnyRate = getRubToCnyRate();
            
            // 获取USD到CNY的汇率
            ExchangeRatesDO usdToCnyRate = getUsdToCnyRate();
            
            // 构建汇率列表
            List<ExchangeRatesDO> exchangeRates = new ArrayList<>();
            if (rubToCnyRate != null) {
                exchangeRates.add(rubToCnyRate);
            }
            if (usdToCnyRate != null) {
                exchangeRates.add(usdToCnyRate);
            }
            
            log.info("收集到汇率数据: {} 条, taskId={}", exchangeRates.size(), taskId);
            
            // 构建汇率映射（基于现有字段结构）
            Map<String, BigDecimal> latestRates = buildCurrentRatesMap(rubToCnyRate, usdToCnyRate);
            
            // 验证关键汇率的可用性
            validateKeyRates(latestRates, taskId);
            
            return ExchangeRateData.builder()
                    .exchangeRates(exchangeRates)
                    .dailyRatesMap(Collections.emptyMap()) // 简化：不按日期分组
                    .latestRates(latestRates)
                    .dateRange(new LocalDate[]{startDate, endDate})
                    .totalRateCount(exchangeRates.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("收集汇率数据失败: taskId={}", taskId, e);
            throw new RuntimeException("汇率数据收集失败", e);
        }
    }
    
    /**
     * 获取RUB到CNY的汇率
     */
    private ExchangeRatesDO getRubToCnyRate() {
        try {
            // 修复：使用字典服务获取正确的货币代码，与V1版本保持一致
            String rubCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB");
            if (rubCurrencyCode == null) {
                log.warn("获取RUB货币代码失败，字典配置可能不存在");
                return null;
            }
            
            log.info("获取RUB汇率，货币代码: {}", rubCurrencyCode);
            return exchangeRatesService.getExchangeRatesByBaseCurrency(Integer.valueOf(rubCurrencyCode));
        } catch (Exception e) {
            log.warn("获取RUB汇率失败，将使用默认汇率", e);
            return null;
        }
    }
    
    /**
     * 获取USD到CNY的汇率
     */
    private ExchangeRatesDO getUsdToCnyRate() {
        try {
            // 修复：使用字典服务获取正确的货币代码，与V1版本保持一致
            String usdCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "USD");
            if (usdCurrencyCode == null) {
                log.warn("获取USD货币代码失败，字典配置可能不存在");
                return null;
            }
            
            log.info("获取USD汇率，货币代码: {}", usdCurrencyCode);
            return exchangeRatesService.getExchangeRatesByBaseCurrency(Integer.valueOf(usdCurrencyCode));
        } catch (Exception e) {
            log.warn("获取USD汇率失败，将使用默认汇率", e);
            return null;
        }
    }
    
    /**
     * 构建当前汇率映射
     */
    private Map<String, BigDecimal> buildCurrentRatesMap(ExchangeRatesDO rubRate, ExchangeRatesDO usdRate) {
        Map<String, BigDecimal> ratesMap = new HashMap<>();
        
        // RUB到CNY汇率
        if (rubRate != null) {
            BigDecimal rate = rubRate.getCustomRate() != null ? rubRate.getCustomRate() : rubRate.getOfficialRate();
            if (rate != null) {
                ratesMap.put("RUB_CNY", rate);
                log.info("成功获取RUB->CNY汇率: {}", rate);
            } else {
                ratesMap.put("RUB_CNY", DEFAULT_RUB_TO_CNY);
                log.warn("RUB汇率数据为空，使用默认汇率: {}", DEFAULT_RUB_TO_CNY);
            }
        } else {
            ratesMap.put("RUB_CNY", DEFAULT_RUB_TO_CNY);
            log.warn("RUB汇率记录为空，使用默认汇率: {}", DEFAULT_RUB_TO_CNY);
        }
        
        // USD到CNY汇率
        if (usdRate != null) {
            BigDecimal rate = usdRate.getCustomRate() != null ? usdRate.getCustomRate() : usdRate.getOfficialRate();
            if (rate != null) {
                ratesMap.put("USD_CNY", rate);
                log.info("成功获取USD->CNY汇率: {}", rate);
            } else {
                ratesMap.put("USD_CNY", DEFAULT_USD_TO_CNY);
                log.warn("USD汇率数据为空，使用默认汇率: {}", DEFAULT_USD_TO_CNY);
            }
        } else {
            ratesMap.put("USD_CNY", DEFAULT_USD_TO_CNY);
            log.warn("USD汇率记录为空，使用默认汇率: {}", DEFAULT_USD_TO_CNY);
        }
        
        return ratesMap;
    }
    
    /**
     * 验证关键汇率的可用性
     */
    private void validateKeyRates(Map<String, BigDecimal> latestRates, String taskId) {
        String[] keyPairs = {"RUB_CNY", "USD_CNY"};
        int missingCount = 0;
        
        for (String pair : keyPairs) {
            BigDecimal rate = latestRates.get(pair);
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                missingCount++;
                log.warn("汇率数据异常: pair={}, rate={}, taskId={}", pair, rate, taskId);
            }
        }
        
        if (missingCount > 0) {
            log.warn("发现 {} 条汇率数据异常，已使用默认汇率补充, taskId={}", missingCount, taskId);
        } else {
            log.info("汇率数据验证通过: {}, taskId={}", latestRates, taskId);
        }
    }
} 