package cn.iocoder.yudao.module.dm.service.calculation.strategy;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 国家计算策略工厂
 * 
 * 遵循工厂模式：根据国家代码获取对应的计算策略
 * 遵循开闭原则：新增国家策略时无需修改工厂代码
 * 遵循依赖注入原则：通过Spring自动注入所有策略实现
 *
 * @author Jax
 */
@Slf4j
@Component
public class CountryCalculationStrategyFactory {

    private final Map<String, CountryCalculationStrategy> strategyMap = new HashMap<>();

    @Autowired
    private List<CountryCalculationStrategy> strategies;

    /**
     * 初始化策略映射
     */
    @PostConstruct
    public void initStrategies() {
        if (strategies != null) {
            for (CountryCalculationStrategy strategy : strategies) {
                String country = strategy.getSupportedCountry();
                if (country != null && !country.trim().isEmpty()) {
                    strategyMap.put(country.toUpperCase(), strategy);
                    log.info("注册国家计算策略: {} -> {}", country, strategy.getClass().getSimpleName());
                }
            }
        }
        
        log.info("国家计算策略工厂初始化完成，共注册{}个策略", strategyMap.size());
    }

    /**
     * 根据国家代码获取计算策略
     *
     * @param countryCode 国家代码（如：US, DE, UK等）
     * @return 对应的计算策略，如果不支持则返回null
     */
    public CountryCalculationStrategy getStrategy(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            log.warn("国家代码为空，无法获取计算策略");
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PROFIT_CALCULATION_COUNTRY_NULL);
        }
        
        CountryCalculationStrategy strategy = strategyMap.get(countryCode.toUpperCase());
        if (strategy == null) {
            log.warn("不支持的国家代码: {}", countryCode);
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PROFIT_CALCULATION_COUNTRY_NOT_SUPPORTED, countryCode);
        }
        
        return strategy;
    }

    /**
     * 获取支持的国家列表
     * 
     * @return 支持的国家代码集合
     */
    public Set<String> getSupportedCountries() {
        return strategyMap.keySet();
    }

    /**
     * 获取指定国家的计算说明
     * 
     * @param countryCode 国家代码
     * @return 计算说明
     */
    public String getCalculationDescription(String countryCode) {
        CountryCalculationStrategy strategy = getStrategy(countryCode);
        if (strategy == null) {
            return "不支持的国家: " + countryCode;
        }
        return strategy.getCalculationDescription();
    }

    /**
     * 获取所有支持国家的计算说明
     * 
     * @return 国家代码到计算说明的映射
     */
    public Map<String, String> getAllCalculationDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        for (Map.Entry<String, CountryCalculationStrategy> entry : strategyMap.entrySet()) {
            descriptions.put(entry.getKey(), entry.getValue().getCalculationDescription());
        }
        return descriptions;
    }

}