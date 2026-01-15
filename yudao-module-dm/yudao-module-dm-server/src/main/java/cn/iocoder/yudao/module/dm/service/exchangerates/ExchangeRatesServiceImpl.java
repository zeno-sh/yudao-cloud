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
        // 1. 获取当前租户下所有汇率记录
        List<ExchangeRatesDO> exchangeRatesList = getAllExchangeRates();
        if (CollUtil.isEmpty(exchangeRatesList)) {
            log.warn("[syncOfficialExchangeRates] 当前租户无汇率记录，跳过同步");
            return 0;
        }

        // 2. 提取币种代码列表
        List<String> currencyCodes = exchangeRatesList.stream()
                .map(ExchangeRatesDO::getCurrencyCode)
                .filter(code -> code != null && !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(currencyCodes)) {
            log.warn("[syncOfficialExchangeRates] 无有效币种代码，跳过同步");
            return 0;
        }

        log.info("[syncOfficialExchangeRates] 开始同步官方汇率，币种数量: {}", currencyCodes.size());

        // 3. 调用外部 API 获取最新汇率
        Map<String, BigDecimal> latestRates = exchangeRateApiClient.getLatestRates(currencyCodes);

        // 4. 遍历更新 officialRate 字段，不修改 customRate
        int updatedCount = 0;
        for (ExchangeRatesDO exchangeRate : exchangeRatesList) {
            String currencyCode = exchangeRate.getCurrencyCode();
            if (currencyCode == null || currencyCode.isEmpty()) {
                continue;
            }

            BigDecimal newRate = latestRates.get(currencyCode.toUpperCase());
            if (newRate == null) {
                log.warn("[syncOfficialExchangeRates] 未获取到币种 {} 的汇率", currencyCode);
                continue;
            }

            // 只更新 officialRate，保留原有的 customRate
            ExchangeRatesDO updateObj = new ExchangeRatesDO();
            updateObj.setId(exchangeRate.getId());
            updateObj.setOfficialRate(newRate);
            exchangeRatesMapper.updateById(updateObj);
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