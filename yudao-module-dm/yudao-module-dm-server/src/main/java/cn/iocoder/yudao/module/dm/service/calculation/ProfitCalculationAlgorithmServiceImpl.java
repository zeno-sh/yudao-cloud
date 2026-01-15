package cn.iocoder.yudao.module.dm.service.calculation;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.dm.api.DmExchangeRateService;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;
import cn.iocoder.yudao.module.dm.service.calculation.strategy.CountryCalculationStrategy;
import cn.iocoder.yudao.module.dm.service.calculation.strategy.CountryCalculationStrategyFactory;
import cn.iocoder.yudao.module.dm.service.template.ProfitCalculationTemplateService;
import cn.iocoder.yudao.module.system.api.currency.ExchangeRateApi;
import cn.iocoder.yudao.module.system.api.currency.dto.ExchangeRateRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 利润计算算法服务实现
 * <p>
 * 遵循单一职责原则：专注于利润计算算法的实现
 * 遵循依赖倒置原则：依赖抽象的策略接口而非具体实现
 * 遵循开闭原则：通过策略模式支持新国家的扩展
 *
 * @author Jax
 */
@Slf4j
@Service
public class ProfitCalculationAlgorithmServiceImpl implements ProfitCalculationAlgorithmService {

    @Resource
    private CountryCalculationStrategyFactory strategyFactory;

    @Resource
    private ProfitCalculationTemplateService templateService;

    @Resource
    private DmExchangeRateService exchangeRateQueryService;

    @Resource
    private ExchangeRateApi exchangeRateApi;


    // 批量计算的线程池
    private final Executor batchCalculationExecutor = Executors.newFixedThreadPool(10);

    // 默认汇率（当无法获取实时汇率时使用）
    private static final BigDecimal DEFAULT_EXCHANGE_RATE = BigDecimal.ONE;

    @Override
    public ProfitCalculationResultDTO calculateProfit(ProfitCalculationSaveReqVO saveReqVO) {
        // 2. 获取计算策略
        CountryCalculationStrategy strategy = strategyFactory.getStrategy(saveReqVO.getCountry());
        if (strategy == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PROFIT_CALCULATION_COUNTRY_NOT_SUPPORTED, saveReqVO.getCountry());
        }

        // 3. 获取配置模板
        ProfitCalculationTemplateDO template = getCalculationTemplate(saveReqVO);

        // 4. 获取汇率
        BigDecimal exchangeRate = getExchangeRate(saveReqVO.getCurrencyCode(), saveReqVO.getExchangeRate());

        return strategy.calculate(saveReqVO, template, exchangeRate);
    }


    @Override
    public List<ProfitCalculationResultDTO> batchCalculateProfit(List<ProfitCalculationSaveReqVO> saveReqVOs) {
        if (CollectionUtils.isEmpty(saveReqVOs)) {
            return new ArrayList<>();
        }

        log.info("开始批量利润计算，共{}个请求", saveReqVOs.size());

        try {
            // 使用并行流进行批量计算
            List<CompletableFuture<ProfitCalculationResultDTO>> futures = saveReqVOs.stream()
                    .map(saveReqVO -> CompletableFuture.supplyAsync(() -> calculateProfit(saveReqVO), batchCalculationExecutor))
                    .collect(Collectors.toList());

            // 等待所有计算完成
            List<ProfitCalculationResultDTO> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            return results;

        } catch (Exception e) {
            log.error("批量利润计算失败: {}", e.getMessage(), e);

            // 返回错误结果列表
            return saveReqVOs.stream()
                    .map(saveReqVO -> ProfitCalculationResultDTO.builder()
                            .productId(saveReqVO.getProductId())
                            .planName(saveReqVO.getPlanName())
                            .platform(saveReqVO.getPlatform())
                            .country(saveReqVO.getCountry())
                            .build())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Set<String> getSupportedCountries() {
        return strategyFactory.getSupportedCountries();
    }

    @Override
    public String getCalculationDescription(String countryCode) {
        return strategyFactory.getCalculationDescription(countryCode);
    }

    @Override
    public java.util.Map<String, String> getAllCalculationDescriptions() {
        return strategyFactory.getAllCalculationDescriptions();
    }

    /**
     * 获取计算模板
     */
    private ProfitCalculationTemplateDO getCalculationTemplate(ProfitCalculationSaveReqVO request) {
        ProfitCalculationTemplateDO template;

        if (request.getTemplateId() != null) {
            // 使用指定的模板ID
            template = templateService.getProfitCalculationTemplate(request.getTemplateId());
            if (template == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.PROFIT_CALCULATION_TEMPLATE_NOT_FOUND, request.getTemplateId());
            }
            return template;
        }
        return null;
    }


    /**
     * 获取汇率，按照优先级:
     * 1. 用户指定汇率
     * 2. DM系统汇率
     * 3. 外部API汇率
     * 4. 默认汇率
     *
     * @param fromCurrency 源币种
     * @param exchangeRate 用户指定汇率
     * @return 最终使用的汇率
     */
    private BigDecimal getExchangeRate(String fromCurrency, BigDecimal exchangeRate) {
        // 1. 优先使用用户指定汇率
        if (exchangeRate != null && exchangeRate.compareTo(BigDecimal.ZERO) > 0) {
            return exchangeRate;
        }

        // 2. 检查币种是否有效
        if (fromCurrency == null || fromCurrency.trim().isEmpty()) {
            log.warn("源币种为空，使用默认汇率：{}", DEFAULT_EXCHANGE_RATE);
            return DEFAULT_EXCHANGE_RATE;
        }

        try {
            // 3. 尝试获取DM系统汇率
            BigDecimal dmExchangeRate = exchangeRateQueryService.getExchangeRate(fromCurrency).getData();
            if (dmExchangeRate != null && dmExchangeRate.compareTo(BigDecimal.ZERO) > 0) {
                return dmExchangeRate;
            }

            // 4. 尝试获取外部API汇率
            ExchangeRateRespDTO rateRespDTO = exchangeRateApi.getExchangeRateByBaseCurrency(fromCurrency).getData();
            if (rateRespDTO != null && rateRespDTO.getRate() != null
                    && rateRespDTO.getRate().compareTo(BigDecimal.ZERO) > 0) {
                return rateRespDTO.getRate();
            }
        } catch (Exception e) {
            log.error("获取汇率异常，币种：{}，使用默认汇率", fromCurrency, e);
        }

        // 5. 兜底使用默认汇率
        log.warn("无法获取币种：{} 的有效汇率，使用默认汇率：{}", fromCurrency, DEFAULT_EXCHANGE_RATE);
        return DEFAULT_EXCHANGE_RATE;
    }

}