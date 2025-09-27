package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 汇率数据
 *
 * @author Jax
 */
@Data
@Builder
public class ExchangeRateData {
    
    /**
     * 汇率记录列表
     */
    private List<ExchangeRatesDO> exchangeRates;
    
    /**
     * 日期到汇率的映射 (date -> (currencyPair -> rate))
     */
    private Map<LocalDate, Map<String, BigDecimal>> dailyRatesMap;
    
    /**
     * 最新汇率映射 (currencyPair -> rate)
     */
    private Map<String, BigDecimal> latestRates;
    
    /**
     * 日期范围 [startDate, endDate]
     */
    private LocalDate[] dateRange;
    
    /**
     * 汇率记录总数
     */
    private Integer totalRateCount;
} 