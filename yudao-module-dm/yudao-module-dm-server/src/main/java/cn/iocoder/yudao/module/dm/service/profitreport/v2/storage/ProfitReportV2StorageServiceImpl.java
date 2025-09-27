package cn.iocoder.yudao.module.dm.service.profitreport.v2.storage;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.module.dm.dal.mysql.profitreport.ProfitReportMapper;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.AggregationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 财务账单报告结果存储服务实现
 * 基于数据层复用设计方案，直接使用ProfitReportDO
 *
 * @author Jax
 */
@Service
@Slf4j
public class ProfitReportV2StorageServiceImpl implements ProfitReportV2StorageService {

    @Resource
    private ProfitReportMapper profitReportMapper;

    private static final int BATCH_SIZE = 500;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveAggregationResult(AggregationResult aggregationResult, String taskId) {
        log.info("开始保存聚合结果到数据库: taskId={}, 记录数={}", 
                taskId, aggregationResult.getTotalRecords());

        // 获取聚合结果列表
        List<Object> aggregatedResults = aggregationResult.getAggregatedResults();
        
        if (aggregatedResults == null || aggregatedResults.isEmpty()) {
            log.warn("聚合结果为空, 无需保存: taskId={}", taskId);
            return 0;
        }

        try {
            // **方案一：直接转换为ProfitReportDO列表，无需复杂转换**
            List<ProfitReportDO> resultDOList = aggregatedResults.stream()
                    .filter(obj -> obj instanceof ProfitReportDO)
                    .map(obj -> (ProfitReportDO) obj)
                    .collect(Collectors.toList());

            // 分批插入
            int totalSaved = 0;
            for (int i = 0; i < resultDOList.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, resultDOList.size());
                List<ProfitReportDO> batch = resultDOList.subList(i, endIndex);
                
                profitReportMapper.insertBatch(batch);
                int batchSaved = batch.size(); // 由于insertBatch可能不返回影响行数，直接使用批次大小
                totalSaved += batchSaved;
                
                log.debug("批次保存完成: batch={}, 保存记录数={}", (i / BATCH_SIZE + 1), batchSaved);
            }

            log.info("聚合结果保存完成: taskId={}, 总保存记录数={}", taskId, totalSaved);
            return totalSaved;

        } catch (Exception e) {
            log.error("保存聚合结果失败: taskId={}", taskId, e);
            throw new RuntimeException("保存聚合结果失败", e);
        }
    }

    @Override
    public PageResult<ProfitReportResultVO> getResultsByPage(ProfitReportQueryReqVO queryReqVO) {
        log.info("分页查询利润报告结果: clientId={}, 查询条件={}", 
                queryReqVO.getClientId(), queryReqVO);

        // 构建查询条件 - 使用原有Mapper的查询方式
        cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportPageReqVO pageReqVO = 
                new cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(queryReqVO.getPageSize() != null ? queryReqVO.getPageSize() : 10);
        
        if (queryReqVO.getClientId() != null) {
            pageReqVO.setClientIds(new String[]{queryReqVO.getClientId()});
        }
        
        if (queryReqVO.getDateRange() != null && queryReqVO.getDateRange().length >= 2) {
            pageReqVO.setFinanceDate(new LocalDate[]{queryReqVO.getDateRange()[0], queryReqVO.getDateRange()[1]});
        }

        // 查询数据库
        PageResult<ProfitReportDO> doPageResult = profitReportMapper.selectPage(pageReqVO);
        
        // 转换为VO
        List<ProfitReportResultVO> voList = doPageResult.getList().stream()
                .map(this::convertToResultVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, doPageResult.getTotal());
    }

    @Override
    public List<ProfitReportResultVO> getResultsList(ProfitReportQueryReqVO queryReqVO) {
        log.info("查询利润报告结果列表: clientId={}, 查询条件={}", 
                queryReqVO.getClientId(), queryReqVO);

        // 构建查询条件 - 使用原有Mapper的查询方式
        cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportPageReqVO pageReqVO = 
                new cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportPageReqVO();
        
        if (queryReqVO.getClientId() != null) {
            pageReqVO.setClientIds(new String[]{queryReqVO.getClientId()});
        }
        
        if (queryReqVO.getDateRange() != null && queryReqVO.getDateRange().length >= 2) {
            pageReqVO.setFinanceDate(new LocalDate[]{queryReqVO.getDateRange()[0], queryReqVO.getDateRange()[1]});
        }

        // 查询数据库
        PageResult<ProfitReportDO> doPageResult = profitReportMapper.selectPage(pageReqVO);
        
        // 转换为VO
        return doPageResult.getList().stream()
                .map(this::convertToResultVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteResultsByTaskId(String taskId) {
        log.info("删除任务结果: taskId={}", taskId);
        
        // 注意：由于ProfitReportDO中没有taskId字段，此方法暂时返回0
        // TODO: 根据实际业务需求，可能需要根据其他条件删除数据
        log.warn("ProfitReportDO中没有taskId字段，无法根据taskId删除数据: taskId={}", taskId);
        
        int deletedCount = 0;
        log.info("任务结果删除完成: taskId={}, 删除记录数={}", taskId, deletedCount);
        return deletedCount;
    }

    @Override
    public Long countResultsByTaskId(String taskId) {
        // 注意：由于ProfitReportDO中没有taskId字段，此方法暂时返回0
        // TODO: 根据实际业务需求，可能需要根据其他条件查询数量
        log.warn("ProfitReportDO中没有taskId字段，无法根据taskId查询数量: taskId={}", taskId);
        return 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredData(int retentionDays) {
        LocalDate beforeDate = LocalDate.now().minusDays(retentionDays);
        
        log.info("清理过期数据: 保留天数={}, 删除日期早于={}", retentionDays, beforeDate);
        
        // 使用原有的删除逻辑 - 根据日期范围删除
        LocalDate[] dateRange = new LocalDate[]{LocalDate.of(1970, 1, 1), beforeDate};
        String[] allClientIds = {"*"}; // 使用通配符表示所有客户
        
        try {
            profitReportMapper.deleteProfitReport(allClientIds, dateRange);
            log.info("过期数据清理完成: 删除日期早于={}", beforeDate);
            return 1; // 由于原有方法不返回数量，返回1表示成功
        } catch (Exception e) {
            log.error("清理过期数据失败", e);
            return 0;
        }
    }

    @Override
    public boolean existsResultsByTaskId(String taskId) {
        Long count = countResultsByTaskId(taskId);
        return count != null && count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteResultsByClientIdAndDate(String clientId, LocalDate startDate, LocalDate endDate) {
        log.info("根据clientId和日期范围删除利润报告结果: clientId={}, startDate={}, endDate={}", clientId, startDate, endDate);
        
        // 使用原有的删除逻辑，保持一致性
        String[] clientIds = {clientId};
        LocalDate[] financeDate = {startDate, endDate};
        
        try {
            profitReportMapper.deleteProfitReport(clientIds, financeDate);
            log.info("历史数据删除完成: clientId={}, dateRange=[{}, {}]", clientId, startDate, endDate);
            return 1; // 由于原有方法不返回数量，返回1表示成功
        } catch (Exception e) {
            log.error("删除历史数据失败: clientId={}, dateRange=[{}, {}]", clientId, startDate, endDate, e);
            throw new RuntimeException("删除历史数据失败", e);
        }
    }

    /**
     * 将DO对象转换为结果VO
     */
    private ProfitReportResultVO convertToResultVO(ProfitReportDO resultDO) {
        // 基础信息
        ProfitReportResultVO resultVO = ProfitReportResultVO.builder()
                .clientId(resultDO.getClientId())
                .productId(resultDO.getProductId()) // 添加productId
                .platformSkuId(resultDO.getPlatformSkuId())
                .offerId(resultDO.getOfferId())
                .financeDate(resultDO.getFinanceDate())
                .orders(resultDO.getOrders())
                .salesVolume(resultDO.getSalesVolume())
                .refundOrders(resultDO.getRefundOrders()) // 添加退货订单数
                .build();
        
        // 销售金额
        if (resultDO.getSalesAmount() != null) {
            resultVO.setSalesAmount(MonetaryAmount.builder()
                    .amount(resultDO.getSalesAmount())
                    .currencyCode(convertCurrencyIntegerToString(resultDO.getPlatformCurrency()))
                    .build());
        }
        
        // 结算金额
        if (resultDO.getSettleAmount() != null) {
            resultVO.setSettleAmount(MonetaryAmount.builder()
                    .amount(resultDO.getSettleAmount())
                    .currencyCode(convertCurrencyIntegerToString(resultDO.getPlatformCurrency()))
                    .build());
        }
        
        // 构建成本明细
        CostBreakdown costBreakdown = CostBreakdown.builder()
                .commission(buildMonetaryAmount(resultDO.getCategoryCommissionCost(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency())))
                .lastMileDelivery(buildMonetaryAmount(resultDO.getLogisticsLastMileCost(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency())))
                .transferCost(buildMonetaryAmount(resultDO.getLogisticsTransferCost(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency())))
                .pickupCost(buildMonetaryAmount(resultDO.getLogisticsDropOff(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency())))
                .orderFee(buildMonetaryAmount(resultDO.getOrderFeeCost(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency())))
                .otherAgentService(buildMonetaryAmount(resultDO.getOtherAgentServiceCost(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency())))
                .purchaseCost(buildMonetaryAmount(resultDO.getPurchaseCost(), convertCurrencyIntegerToString(resultDO.getPurchaseCurrency())))
                .purchaseShipping(buildMonetaryAmount(resultDO.getPurchaseShippingCost(), convertCurrencyIntegerToString(resultDO.getPurchaseCurrency())))
                .firstMileShipping(buildMonetaryAmount(resultDO.getLogisticsShippingCost(), convertCurrencyIntegerToString(resultDO.getLogisticsCurrency())))
                .fbsStorageFee(buildMonetaryAmount(addBigDecimal(resultDO.getFbsCheckInCost(), resultDO.getFbsOperatingCost(), resultDO.getFbsOtherCost()), convertCurrencyIntegerToString(resultDO.getFbsCurrency())))
                .fboDeliveryFee(buildMonetaryAmount(resultDO.getFboDeliverCost(), convertCurrencyIntegerToString(resultDO.getFbsCurrency())))
                .fboInspectionFee(buildMonetaryAmount(resultDO.getFboInspectionCost(), convertCurrencyIntegerToString(resultDO.getFbsCurrency())))
                .salesVat(buildMonetaryAmount(resultDO.getSalesVatCost(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency())))
                .customsDuty(buildMonetaryAmount(resultDO.getCustomsCost(), convertCurrencyIntegerToString(resultDO.getCustomsCurrency())))
                .importVat(buildMonetaryAmount(resultDO.getVatCost(), convertCurrencyIntegerToString(resultDO.getCustomsCurrency())))
                .reverseLogistics(buildMonetaryAmount(resultDO.getReverseLogisticsCost(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency()))) // 添加逆向物流
                .refundAmount(buildMonetaryAmount(resultDO.getRefundAmount(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency()))) // 添加赔偿金额
                .returnCommission(buildMonetaryAmount(resultDO.getReturnCommissionAmount(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency()))) // 添加退还佣金
                .cancelledAmount(buildMonetaryAmount(resultDO.getCancelledAmount(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency()))) // 添加取消金额
                .compensationAmount(buildMonetaryAmount(resultDO.getCompensationAmount(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency()))) // 添加平台补偿金额
                .build();
        resultVO.setCostBreakdown(costBreakdown);

        // 构建利润指标
        BigDecimal grossProfitRate = calculateGrossProfitRate(resultDO.getProfitAmount(), resultDO.getSalesAmount());
        ProfitMetrics profitMetrics = ProfitMetrics.builder()
                .grossProfit(buildMonetaryAmount(resultDO.getProfitAmount(), convertCurrencyIntegerToString(resultDO.getPlatformCurrency())))
                .grossProfitRate(grossProfitRate)
                .build();
        resultVO.setProfitMetrics(profitMetrics);

        // 构建计算元数据
        CalculationMetadata metadata = CalculationMetadata.builder()
                .calculationType("aggregated")
                .calculationTime(LocalDateTime.now())
                .calculationVersion("v2.0")
                .dataSource("OZON_API")
                .precision(4)
                .build();
        resultVO.setMetadata(metadata);

        return resultVO;
    }

    /**
     * 安全的BigDecimal加法
     */
    private BigDecimal addBigDecimal(BigDecimal... values) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (value != null) {
                result = result.add(value);
            }
        }
        return result;
    }

    /**
     * 从MonetaryAmount中提取金额
     */
    private java.math.BigDecimal getAmountFromMonetary(MonetaryAmount monetaryAmount) {
        return monetaryAmount != null ? monetaryAmount.getAmount() : null;
    }

    /**
     * 构建MonetaryAmount对象
     */
    private MonetaryAmount buildMonetaryAmount(java.math.BigDecimal amount, String currency) {
        if (amount == null) {
            return null;
        }
        
        return MonetaryAmount.builder()
                .amount(amount)
                .currencyCode(currency != null ? currency : "CNY")
                .build();
    }

    /**
     * 币种字符串转换为整数（根据字典表）
     */
    private Integer convertCurrencyStringToInteger(String currencyCode) {
        if (currencyCode == null) {
            return null;
        }
        // 这里应该根据实际的字典表进行转换
        switch (currencyCode) {
            case "CNY":
                return 1;
            case "USD": 
                return 2;
            case "RUB":
                return 3;
            default:
                return 1; // 默认CNY
        }
    }

    /**
     * 币种整数转换为字符串（根据字典表）
     */
    private String convertCurrencyIntegerToString(Integer currencyId) {
        if (currencyId == null) {
            return "CNY";
        }
        // 这里应该根据实际的字典表进行转换
        switch (currencyId) {
            case 1:
                return "CNY";
            case 2:
                return "USD";
            case 3:
                return "RUB";
            default:
                return "CNY";
        }
    }

    /**
     * 计算毛利率
     */
    private java.math.BigDecimal calculateGrossProfitRate(java.math.BigDecimal profitAmount, java.math.BigDecimal salesAmount) {
        if (profitAmount == null || salesAmount == null || salesAmount.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return java.math.BigDecimal.ZERO;
        }
        
        return profitAmount.divide(salesAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new java.math.BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}