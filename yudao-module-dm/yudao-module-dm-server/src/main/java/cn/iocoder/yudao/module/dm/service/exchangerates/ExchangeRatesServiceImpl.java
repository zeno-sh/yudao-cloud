package cn.iocoder.yudao.module.dm.service.exchangerates;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.dm.service.exchangerates.client.ExchangeRateApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import cn.iocoder.yudao.module.dm.controller.admin.exchangerates.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.module.system.api.currency.CurrencyApi;
import cn.iocoder.yudao.module.system.api.currency.dto.CurrencyRespDTO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.exchangerates.ExchangeRatesMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 汇率 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
@Slf4j
public class ExchangeRatesServiceImpl implements ExchangeRatesService {

    @Resource
    private ExchangeRatesMapper exchangeRatesMapper;

    @Override
    public Long createExchangeRates(ExchangeRatesSaveReqVO createReqVO) {
        // 插入
        ExchangeRatesDO exchangeRates = BeanUtils.toBean(createReqVO, ExchangeRatesDO.class);
        exchangeRatesMapper.insert(exchangeRates);
        // 返回
        return exchangeRates.getId();
    }

    @Override
    public void updateExchangeRates(ExchangeRatesSaveReqVO updateReqVO) {
        // 校验存在
        validateExchangeRatesExists(updateReqVO.getId());
        // 更新
        ExchangeRatesDO updateObj = BeanUtils.toBean(updateReqVO, ExchangeRatesDO.class);
        exchangeRatesMapper.updateById(updateObj);
    }

    @Override
    public void deleteExchangeRates(Long id) {
        // 校验存在
        validateExchangeRatesExists(id);
        // 删除
        exchangeRatesMapper.deleteById(id);
    }

    private void validateExchangeRatesExists(Long id) {
        if (exchangeRatesMapper.selectById(id) == null) {
            throw exception(EXCHANGE_RATES_NOT_EXISTS);
        }
    }

    @Override
    public ExchangeRatesDO getExchangeRates(Long id) {
        return exchangeRatesMapper.selectById(id);
    }

    @Override
    public PageResult<ExchangeRatesDO> getExchangeRatesPage(ExchangeRatesPageReqVO pageReqVO) {
        return exchangeRatesMapper.selectPage(pageReqVO);
    }

    @Override
    public ExchangeRatesDO getExchangeRatesByCurrencyCode(String currencyCode) {
        LambdaQueryWrapperX<ExchangeRatesDO> queryWrapperX = new LambdaQueryWrapperX<ExchangeRatesDO>()
                .eqIfPresent(ExchangeRatesDO::getCurrencyCode, currencyCode);
        return exchangeRatesMapper.selectOne(queryWrapperX);
    }

    @Resource
    private ExchangeRateApiClient exchangeRateApiClient;

    @Resource
    private CurrencyApi currencyApi;

    @Override
    public void batchInitExchangeRates(Long tenantId, List<String> currencyCodes) {
        if (CollUtil.isEmpty(currencyCodes)) {
            log.warn("[batchInitExchangeRates] 币种列表为空，跳过初始化");
            return;
        }

        log.info("[batchInitExchangeRates] 开始为租户({}) 初始化汇率，币种数量: {}", tenantId, currencyCodes.size());

        // 1. 调用外部 API 获取实时汇率
        Map<String, BigDecimal> rates = exchangeRateApiClient.getLatestRates(currencyCodes);

        // 2. 构建汇率记录
        List<ExchangeRatesDO> list = currencyCodes.stream().map(code -> {
            // API 失败时使用默认值 1.0
            BigDecimal rate = rates.getOrDefault(code.toUpperCase(), BigDecimal.ONE);
            return ExchangeRatesDO.builder()
                    .currencyCode(code)
                    .officialRate(rate) // 官方汇率
                    .customRate(rate) // 自定义汇率初始化为与官方汇率相同
                    .build();
        }).collect(Collectors.toList());

        // 3. 在指定租户上下文中批量插入
        TenantUtils.execute(tenantId, () -> exchangeRatesMapper.insertBatch(list));

        log.info("[batchInitExchangeRates] 租户({}) 汇率初始化完成，共创建 {} 条记录", tenantId, list.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncOfficialExchangeRates() {
        // 1. 获取系统启用的币种列表
        CommonResult<List<CurrencyRespDTO>> enableCurrenciesResult = currencyApi.getEnabledCurrencyList();
        if (enableCurrenciesResult.isError()) {
            log.error("[syncOfficialExchangeRates] 获取启用币种失败: {}", enableCurrenciesResult.getMsg());
            return 0;
        }
        List<CurrencyRespDTO> enableCurrencies = enableCurrenciesResult.getData();
        if (CollUtil.isEmpty(enableCurrencies)) {
            log.warn("[syncOfficialExchangeRates] 系统无启用币种，跳过同步");
            return 0;
        }

        // 2. 提取币种代码列表
        List<String> currencyCodes = enableCurrencies.stream()
                .map(CurrencyRespDTO::getCurrencyCode)
                .filter(code -> code != null && !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        log.info("[syncOfficialExchangeRates] 开始同步官方汇率，币种数量: {}", currencyCodes.size());

        // 3. 调用外部 API 获取最新汇率
        Map<String, BigDecimal> latestRates = exchangeRateApiClient.getLatestRates(currencyCodes);
        if (CollUtil.isEmpty(latestRates)) {
            log.warn("[syncOfficialExchangeRates] 未获取到有效汇率");
            return 0;
        }

        // 4. 更新或插入汇率
        int updatedCount = 0;
        for (String code : latestRates.keySet()) {
            ExchangeRatesDO existDO = getExchangeRatesByCurrencyCode(code);
            BigDecimal newRate = latestRates.get(code);

            if (existDO != null) {
                // 只更新 officialRate
                ExchangeRatesDO updateObj = new ExchangeRatesDO();
                updateObj.setId(existDO.getId());
                updateObj.setOfficialRate(newRate);
                exchangeRatesMapper.updateById(updateObj);
            } else {
                // 插入新记录
                ExchangeRatesDO newDO = ExchangeRatesDO.builder()
                        .currencyCode(code)
                        .officialRate(newRate)
                        .customRate(newRate) // 默认自定义汇率等于官方汇率
                        .build();
                exchangeRatesMapper.insert(newDO);
            }
            updatedCount++;
        }

        log.info("[syncOfficialExchangeRates] 官方汇率同步完成，共更新 {} 条记录", updatedCount);
        return updatedCount;
    }

    @Override
    public List<ExchangeRatesDO> getAllExchangeRates() {
        return exchangeRatesMapper.selectList(new LambdaQueryWrapperX<>());
    }
}