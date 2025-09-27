package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportRespVO;
import cn.iocoder.yudao.module.dm.controller.admin.transaction.vo.OzonFinanceTransactionPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.transaction.vo.OzonServiceTransactionRespVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ClientSimpleInfoDTO;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdCampaignsItemService;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdCampaignsService;
import cn.iocoder.yudao.module.dm.service.exchangerates.ExchangeRatesService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.transaction.OzonFinanceTransactionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.CostBreakdown;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.MonetaryAmount;

/**
 * @author: Zeno
 * @createTime: 2024/10/15 22:14
 */
@Service
@Slf4j
public class OzonProfitService {

    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private OzonFinanceTransactionService ozonFinanceTransactionService;
    @Resource
    private ExchangeRatesService exchangeRatesService;
    @Resource
    private ProductInfoService productInfoService;
    @Resource
    private OzonAdCampaignsItemService ozonAdCampaignsItemService;

    private BigDecimal RUB_EXCHANGE_RATE = new BigDecimal("0.074");
    private BigDecimal USD_EXCHANGE_RATE = new BigDecimal("7.018");

    public void handleClientProfitReport(ProfitReportPageReqVO pageReqVO, List<ProfitReportRespVO> records) {
        // 快速返回空结果
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        
        long startTime = System.currentTimeMillis();
        log.info("开始处理客户端利润报告，记录数: {}", records.size());
        
        try {
            // 1. 批量预加载所有需要的基础数据
            CompletableFuture<Map<String, ClientSimpleInfoDTO>> shopMappingFuture = CompletableFuture.supplyAsync(() -> {
                List<String> clientIds = convertList(records, ProfitReportRespVO::getClientId);
                return ozonShopMappingService.batchSimpleInfoByClientIds(clientIds);
            });

            CompletableFuture<Void> exchangeRateFuture = CompletableFuture.runAsync(this::loadExchangeRates);

            CompletableFuture<Map<String, OzonAdCampaignsItemDO>> adDataFuture =
                CompletableFuture.supplyAsync(() -> loadAdData(pageReqVO));

            CompletableFuture<Map<String, List<OzonFinanceTransactionDO>>> serviceDataFuture =
                CompletableFuture.supplyAsync(() -> loadServiceData(pageReqVO));

            CompletableFuture<Map<String, List<OzonFinanceTransactionDO>>> compensationDataFuture =
                CompletableFuture.supplyAsync(() -> loadCompensationData(pageReqVO));

            // 2. 等待所有异步任务完成
            CompletableFuture.allOf(shopMappingFuture, exchangeRateFuture, adDataFuture, serviceDataFuture, compensationDataFuture).join();

            Map<String, ClientSimpleInfoDTO> shopMappingDOMap = shopMappingFuture.get();
            Map<String, OzonAdCampaignsItemDO> adMap = adDataFuture.get();
            Map<String, List<OzonFinanceTransactionDO>> serviceMap = serviceDataFuture.get();
            Map<String, List<OzonFinanceTransactionDO>> compensationMap = compensationDataFuture.get();

            // 3. 缓存字典值，避免重复查询
            Integer rubCurrency = Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB"));
            Integer cnyCode = Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY"));

            // 4. 批量处理所有记录
            records.forEach(vo -> {
                processRecord(vo, shopMappingDOMap, adMap, serviceMap, compensationMap, rubCurrency, cnyCode);
            });

            long endTime = System.currentTimeMillis();
            log.info("客户端利润报告处理完成，耗时: {}ms", endTime - startTime);
            
        } catch (Exception e) {
            log.error("处理客户端利润报告出错", e);
        }
    }

    /**
     * 加载汇率数据
     */
    private void loadExchangeRates() {
        try {
            String rubCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB");
            String usdCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "USD");

            ExchangeRatesDO rubExchangeRatesDO = exchangeRatesService.getExchangeRatesByBaseCurrency(Integer.valueOf(rubCurrencyCode));
            ExchangeRatesDO usdExchangeRatesDO = exchangeRatesService.getExchangeRatesByBaseCurrency(Integer.valueOf(usdCurrencyCode));
            
            if (rubExchangeRatesDO != null && rubExchangeRatesDO.getCustomRate() != null) {
                RUB_EXCHANGE_RATE = rubExchangeRatesDO.getCustomRate();
            }
            if (usdExchangeRatesDO != null && usdExchangeRatesDO.getCustomRate() != null) {
                USD_EXCHANGE_RATE = usdExchangeRatesDO.getCustomRate();
            }
        } catch (Exception e) {
            log.warn("加载汇率数据失败，使用默认值", e);
        }
    }

    /**
     * 加载广告数据
     */
    private Map<String, OzonAdCampaignsItemDO> loadAdData(ProfitReportPageReqVO pageReqVO) {
        try {
            if (pageReqVO.getFinanceDate() != null && pageReqVO.getFinanceDate().length == 2) {
                List<String> clientIdList = Arrays.asList(pageReqVO.getClientIds());
                LocalDate startDate = pageReqVO.getFinanceDate()[0];
                LocalDate endDate = pageReqVO.getFinanceDate()[1];
                
                // 获取原始按天分组的广告数据
                Map<String, OzonAdCampaignsItemDO> originalAdMap = ozonAdCampaignsItemService.getAdCostByInProcessDateForClientGroupByDate(clientIdList, startDate, endDate);
                
                // 根据groupType重新分组广告数据
                return regroupAdDataByGroupType(originalAdMap, pageReqVO.getGroupType());
            }
        } catch (Exception e) {
            log.warn("加载广告数据失败", e);
        }
        return new HashMap<>();
    }
    
    /**
     * 根据groupType重新分组广告数据
     */
    private Map<String, OzonAdCampaignsItemDO> regroupAdDataByGroupType(Map<String, OzonAdCampaignsItemDO> originalAdMap, String groupType) {
        if ("day".equals(groupType) || originalAdMap.isEmpty()) {
            return originalAdMap; // day类型或空数据直接返回
        }
        
        Map<String, OzonAdCampaignsItemDO> regroupedMap = new HashMap<>();
        
        // 按新的key分组并聚合数据
        for (Map.Entry<String, OzonAdCampaignsItemDO> entry : originalAdMap.entrySet()) {
            String originalKey = entry.getKey(); // 格式: "2024-01-15_clientId"
            OzonAdCampaignsItemDO adData = entry.getValue();
            
            // 解析原始key
            String[] keyParts = originalKey.split("_");
            if (keyParts.length < 2) continue;
            
            String dateStr = keyParts[0];
            String clientId = keyParts[1];
            
            try {
                LocalDate date = LocalDate.parse(dateStr);
                String newDateKey = formatDateByGroupType(date, groupType);
                String newKey = newDateKey + "_" + clientId;
                
                if (regroupedMap.containsKey(newKey)) {
                    // 聚合数据
                    OzonAdCampaignsItemDO existing = regroupedMap.get(newKey);
                    existing.setMoneySpent(existing.getMoneySpent().add(adData.getMoneySpent()));
                    existing.setOrdersMoney(existing.getOrdersMoney().add(adData.getOrdersMoney()));
                    existing.setOrders(existing.getOrders() + adData.getOrders());
                    existing.setViews(existing.getViews() + adData.getViews());
                    existing.setClicks(existing.getClicks() + adData.getClicks());
                } else {
                    // 创建新的聚合数据
                    OzonAdCampaignsItemDO newAdData = new OzonAdCampaignsItemDO();
                    newAdData.setClientId(adData.getClientId());
                    newAdData.setDate(date); // 保持原始日期类型
                    newAdData.setMoneySpent(adData.getMoneySpent());
                    newAdData.setOrdersMoney(adData.getOrdersMoney());
                    newAdData.setOrders(adData.getOrders());
                    newAdData.setViews(adData.getViews());
                    newAdData.setClicks(adData.getClicks());
                    regroupedMap.put(newKey, newAdData);
                }
            } catch (Exception e) {
                log.warn("解析广告数据日期失败: {}", dateStr, e);
            }
        }
        
        return regroupedMap;
    }
    
    /**
     * 根据groupType格式化日期
     */
    private String formatDateByGroupType(LocalDate date, String groupType) {
        if (date == null) {
            return "";
        }
        
        switch (groupType) {
            case "week":
                // 使用与SQL相同的YEARWEEK逻辑：YEARWEEK(date, 1)
                return String.valueOf(date.getYear() * 100 + date.get(java.time.temporal.WeekFields.ISO.weekOfYear()));
            case "month":
                // 使用与SQL相同的格式：DATE_FORMAT(date, '%Y-%m')
                return date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            case "day":
            default:
                // 使用与SQL相同的格式：DATE(date)
                return date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    /**
     * 加载服务数据
     */
    private Map<String, List<OzonFinanceTransactionDO>> loadServiceData(ProfitReportPageReqVO pageReqVO) {
        try {
            OzonFinanceTransactionPageReqVO reqVO = new OzonFinanceTransactionPageReqVO();
            reqVO.setClientIds(pageReqVO.getClientIds());
            reqVO.setGroupType(pageReqVO.getGroupType());
            reqVO.setOperationDate(pageReqVO.getFinanceDate());

            List<OzonFinanceTransactionDO> allServices = ozonFinanceTransactionService.getAllServices(reqVO);
            if (CollectionUtils.isNotEmpty(allServices)) {
                // 根据groupType返回不同格式的key，与财务报告的分组格式保持一致
                return convertMultiMap(allServices, item -> formatDateByGroupType(item.getOperationDate(), pageReqVO.getGroupType()));
            }
        } catch (Exception e) {
            log.warn("加载服务数据失败", e);
        }
        return new HashMap<>();
    }

    /**
     * 加载补偿数据
     */
    private Map<String, List<OzonFinanceTransactionDO>> loadCompensationData(ProfitReportPageReqVO pageReqVO) {
        try {
            List<OzonFinanceTransactionDO> compensationTransactions = ozonFinanceTransactionService.getCompensationTransactions(pageReqVO);
            if (CollectionUtils.isNotEmpty(compensationTransactions)) {
                // 根据groupType返回不同格式的key，与财务报告的分组格式保持一致
                return convertMultiMap(compensationTransactions, item -> formatDateByGroupType(item.getOperationDate(), pageReqVO.getGroupType()));
            }
        } catch (Exception e) {
            log.warn("加载补偿数据失败", e);
        }
        return new HashMap<>();
    }

    /**
     * 处理单条记录
     */
    private void processRecord(ProfitReportRespVO vo, 
                              Map<String, ClientSimpleInfoDTO> shopMappingDOMap,
                              Map<String, OzonAdCampaignsItemDO> adMap,
                              Map<String, List<OzonFinanceTransactionDO>> serviceMap,
                              Map<String, List<OzonFinanceTransactionDO>> compensationMap,
                              Integer rubCurrency, Integer cnyCode) {
        
        // 初始化默认值
        vo.setAdCost(BigDecimal.ZERO);
        vo.setAdOrders(0);
        vo.setAdAmount(BigDecimal.ZERO);
        vo.setPlatformServiceCost(BigDecimal.ZERO);
        
        // 设置店铺信息
        ClientSimpleInfoDTO clientInfo = shopMappingDOMap.get(vo.getClientId());
        if (clientInfo != null) {
            vo.setShopName(clientInfo.getShopName());
            vo.setPlatform(clientInfo.getPlatform());
            vo.setPlatformName(clientInfo.getPlatformName());
        }

        // 设置币种
        if (vo.getPlatformCurrency() == null) {
            vo.setPlatformCurrency(rubCurrency);
        }
        if (vo.getFbsCurrency() == null) {
            vo.setFbsCurrency(cnyCode);
        }
        if (vo.getPurchaseCurrency() == null) {
            vo.setPurchaseCurrency(cnyCode);
        }
        if (vo.getLogisticsCurrency() == null) {
            vo.setLogisticsCurrency(cnyCode);
        }
        if (vo.getCustomsCurrency() == null) {
            vo.setCustomsCurrency(cnyCode);
        }

        // 处理广告数据，使用日期分组的key
        String adKey = vo.getFinanceDate() + "_" + vo.getClientId();
        OzonAdCampaignsItemDO adData = adMap.get(adKey);
        if (adData != null) {
            BigDecimal adAmount = adData.getOrdersMoney() != null ? adData.getOrdersMoney() : BigDecimal.ZERO;
            Integer adOrders = adData.getOrders() != null ? adData.getOrders() : 0;
            vo.setAdOrders(adOrders);
            vo.setAdAmount(convertPrice(vo.getPlatformCurrency(), adAmount));
        }

        // 处理服务费用
        List<OzonFinanceTransactionDO> serviceTransactions = serviceMap.get(vo.getFinanceDate());
        if (serviceTransactions != null) {
            BigDecimal totalServiceCost = serviceTransactions.stream()
                    .filter(service -> vo.getClientId().equals(service.getClientId()))
                    .map(OzonFinanceTransactionDO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<String> serviceTypes = Lists.newArrayList();
            serviceTypes.add("OperationElectronicServiceStencil");
            serviceTypes.add("OperationGettingToTheTop");
            BigDecimal adCost = serviceTransactions.stream()
                    .filter(service -> serviceTypes.contains(service.getOperationType()))
                    .map(OzonFinanceTransactionDO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            vo.setAdCost(adCost.negate());
            vo.setPlatformServiceCost(totalServiceCost.subtract(adCost).negate());
        }

        // 处理补偿金额
        List<OzonFinanceTransactionDO> compensationTransactions = compensationMap.get(vo.getFinanceDate());
        if (compensationTransactions != null) {
            BigDecimal totalCompensationAmount = compensationTransactions.stream()
                    .filter(compensation -> vo.getClientId().equals(compensation.getClientId()))
                    .map(OzonFinanceTransactionDO::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            vo.setCompensationAmount(totalCompensationAmount);
        }

        // 计算指标
        vo.setAcos(safeDivide(vo.getAdCost().abs(), vo.getAdAmount().abs()));
        vo.setAcoas(safeDivide(vo.getAdCost().abs(), vo.getSalesAmount().abs()));

        // 计算结算金额和利润 - 优化计算逻辑
        calculateProfitMetrics(vo);
    }

    /**
     * 计算利润相关指标
     */
    private void calculateProfitMetrics(ProfitReportRespVO vo) {
        // 结算金额计算
        BigDecimal totalSettAmount = vo.getSalesAmount()
                .add(vo.getReturnCommissionAmount())
                .subtract(vo.getCancelledAmount().abs())
                .subtract(vo.getCategoryCommissionCost().abs())
                .subtract(vo.getLogisticsTransferCost().abs())
                .subtract(vo.getLogisticsLastMileCost().abs())
                .subtract(vo.getLogisticsDropOff().abs())
                .subtract(vo.getReverseLogisticsCost().abs())
                .subtract(vo.getRefundAmount().abs())
                .subtract(vo.getAdCost().abs())
                .subtract(vo.getOtherAgentServiceCost().abs())
                .subtract(vo.getPlatformServiceCost().abs())
                .add(vo.getOrderFeeCost()); // 有正数和负数所以需要加法
        vo.setSettleAmount(totalSettAmount);

        // 转换为人民币进行利润计算
        BigDecimal saleAmountCNY = convertToCNY(vo.getPlatformCurrency(), vo.getSalesAmount());
        BigDecimal totalSettAmountCNY = convertToCNY(vo.getPlatformCurrency(), totalSettAmount);
        
        // 计算总成本（人民币）
        BigDecimal totalCostCNY = BigDecimal.ZERO
                .add(convertToCNY(vo.getFbsCurrency(), vo.getFboDeliverCost()).abs())
                .add(convertToCNY(vo.getFbsCurrency(), vo.getFboInspectionCost()).abs())
                .add(convertToCNY(vo.getFbsCurrency(), vo.getFbsCheckInCost()).abs())
                .add(convertToCNY(vo.getFbsCurrency(), vo.getFbsOperatingCost()).abs())
                .add(convertToCNY(vo.getFbsCurrency(), vo.getFbsOtherCost()).abs())
                .add(convertToCNY(vo.getPurchaseCurrency(), vo.getPurchaseCost()).abs())
                .add(convertToCNY(vo.getPurchaseCurrency(), vo.getPurchaseShippingCost()).abs())
                .add(convertToCNY(vo.getLogisticsCurrency(), vo.getLogisticsShippingCost()).abs())
                .add(convertToCNY(vo.getPlatformCurrency(), vo.getSalesVatCost()).abs())
                .add(convertToCNY(vo.getCustomsCurrency(), vo.getVatCost()).abs())
                .add(convertToCNY(vo.getCustomsCurrency(), vo.getCustomsCost()).abs());

        // 毛利润
        BigDecimal profitAmount = totalSettAmountCNY.subtract(totalCostCNY);
        vo.setProfitAmount(profitAmount);

        // 毛利率
        vo.setProfitRate(safeDivide(profitAmount, saleAmountCNY));

        // ROI
        BigDecimal investmentCost = convertToCNY(vo.getPurchaseCurrency(), vo.getPurchaseCost())
                .add(convertToCNY(vo.getLogisticsCurrency(), vo.getLogisticsShippingCost()));
        vo.setRoi(safeDivide(profitAmount, investmentCost.abs()));
    }

    /**
     * 降级处理方法
     */
    private void handleClientProfitReportFallback(ProfitReportPageReqVO pageReqVO, List<ProfitReportRespVO> records) {
        log.warn("使用降级处理逻辑");
        // 这里可以实现简化版的处理逻辑，或者原有逻辑
        List<String> clientIds = convertList(records, ProfitReportRespVO::getClientId);
        Map<String, ClientSimpleInfoDTO> shopMappingDOMap = ozonShopMappingService.batchSimpleInfoByClientIds(clientIds);

        // 预先加载汇率信息，避免多次查询
        String rubCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB");
        String usdCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "USD");

        // 先加载汇率数据，避免每次计算时都查询
        ExchangeRatesDO rubExchangeRatesDO = exchangeRatesService.getExchangeRatesByBaseCurrency(Integer.valueOf(rubCurrencyCode));
        ExchangeRatesDO usdExchangeRatesDO = exchangeRatesService.getExchangeRatesByBaseCurrency(Integer.valueOf(usdCurrencyCode));
        if (rubCurrencyCode != null && rubExchangeRatesDO.getCustomRate() != null) {
            RUB_EXCHANGE_RATE = rubExchangeRatesDO.getCustomRate();
        }
        if (usdCurrencyCode != null && usdExchangeRatesDO.getCustomRate() != null) {
            USD_EXCHANGE_RATE = usdExchangeRatesDO.getCustomRate();
        }

        // 获取广告数据 - 改为根据签收订单的接单日期查询，按日期分组
        final Map<String, cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO> adMap;
        if (pageReqVO.getFinanceDate() != null && pageReqVO.getFinanceDate().length == 2) {
            List<String> clientIdList = Arrays.asList(pageReqVO.getClientIds());
            LocalDate startDate = pageReqVO.getFinanceDate()[0];
            LocalDate endDate = pageReqVO.getFinanceDate()[1];
            
            // 根据签收订单的接单日期查询广告费用，按日期分组
            adMap = ozonAdCampaignsItemService.getAdCostByInProcessDateForClientGroupByDate(clientIdList, startDate, endDate);
        } else {
            adMap = new HashMap<>();
        }

        // 获取其他服务费
        OzonFinanceTransactionPageReqVO ozonFinanceTransactionPageReqVO = new OzonFinanceTransactionPageReqVO();
        ozonFinanceTransactionPageReqVO.setClientIds(pageReqVO.getClientIds());
        ozonFinanceTransactionPageReqVO.setGroupType(pageReqVO.getGroupType());
        ozonFinanceTransactionPageReqVO.setOperationDate(pageReqVO.getFinanceDate());
        ozonFinanceTransactionPageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        IPage<OzonServiceTransactionRespVO> servicePageResult = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        ozonFinanceTransactionService.selectServicePage(servicePageResult, ozonFinanceTransactionPageReqVO);
        Map<String, List<OzonServiceTransactionRespVO>> servcieMap = new HashMap<>();
        List<OzonServiceTransactionRespVO> serviceRecords = servicePageResult.getRecords();
        if (CollectionUtils.isNotEmpty(serviceRecords)) {
            servcieMap.putAll(convertMultiMap(serviceRecords, OzonServiceTransactionRespVO::getOperationDate));
        }

        records.forEach(vo -> {
            vo.setAdCost(BigDecimal.ZERO);
            vo.setAdOrders(0);
            vo.setAdAmount(BigDecimal.ZERO);
            vo.setPlatformServiceCost(BigDecimal.ZERO);
            if (shopMappingDOMap.containsKey(vo.getClientId())) {
                ClientSimpleInfoDTO clientSimpleInfoDTO = shopMappingDOMap.get(vo.getClientId());
                vo.setShopName(clientSimpleInfoDTO.getShopName());
                vo.setPlatform(clientSimpleInfoDTO.getPlatform());
                vo.setPlatformName(clientSimpleInfoDTO.getPlatformName());
            }

            // 设置固定币种 - 平台收入支出固定为卢布
            // 使用VO中的币种字段，如果为空则使用默认值
            if (vo.getPlatformCurrency() == null) {
                vo.setPlatformCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB")));
            }

            // 根据签收订单的接单日期查询广告费用
            if (adMap.containsKey(vo.getClientId())) {
                cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO adData = adMap.get(vo.getClientId());
                BigDecimal adAmount = adData.getOrdersMoney() != null ? adData.getOrdersMoney() : BigDecimal.ZERO;
                Integer adOrders = adData.getOrders() != null ? adData.getOrders() : 0;
                BigDecimal adSpendCost = adData.getMoneySpent() != null ? adData.getMoneySpent() : BigDecimal.ZERO;
                
                vo.setAdOrders(adOrders);
                vo.setAdAmount(convertPrice(vo.getPlatformCurrency(), adAmount));
                vo.setAdCost(convertPrice(vo.getPlatformCurrency(), adSpendCost.negate()));
            }

            String financeDate = vo.getFinanceDate();
            if (servcieMap.containsKey(financeDate)) {
                List<OzonServiceTransactionRespVO> ozonServiceTransactionRespVOS = servcieMap.get(financeDate);
                for (OzonServiceTransactionRespVO ozonServiceTransactionRespVO : ozonServiceTransactionRespVOS) {
                    if (ozonServiceTransactionRespVO.getClientId().equals(vo.getClientId())) {
                        vo.setPlatformServiceCost(convertPrice(vo.getPlatformCurrency(), vo.getPlatformServiceCost().add(ozonServiceTransactionRespVO.getAmount().setScale(0, RoundingMode.HALF_UP))));
                    }
                }
            }

            vo.setAcos(safeDivide(vo.getAdCost().abs(), vo.getAdAmount().abs()));
            vo.setAcoas(safeDivide(vo.getAdCost().abs(), vo.getSalesAmount().abs()));

            // 结算金额=销售金额+退还佣金-取消金额-类目佣金-送货费-收单-逆向物流-赔偿-广告花费-平台服务费
            BigDecimal totalSettAmount = vo.getSalesAmount()
                    .add(vo.getReturnCommissionAmount())
                    .subtract(vo.getCancelledAmount().abs())
                    .subtract(vo.getCategoryCommissionCost().abs())
                    .subtract(vo.getLogisticsTransferCost().abs())
                    .subtract(vo.getLogisticsLastMileCost().abs())
                    .subtract(vo.getLogisticsDropOff().abs())
                    .add(vo.getOrderFeeCost()) // 有正数和负数所以需要加法
                    .subtract(vo.getReverseLogisticsCost().abs())
                    .subtract(vo.getRefundAmount().abs())
                    .subtract(vo.getAdCost().abs())
                    .subtract(vo.getPlatformServiceCost().abs());
            vo.setSettleAmount(totalSettAmount);

            // 确保币种字段有值
            if (vo.getFbsCurrency() == null) {
                vo.setFbsCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY")));
            }
            if (vo.getPurchaseCurrency() == null) {
                vo.setPurchaseCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY")));
            }
            if (vo.getLogisticsCurrency() == null) {
                vo.setLogisticsCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY")));
            }
            if (vo.getCustomsCurrency() == null) {
                vo.setCustomsCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY")));
            }

            // 转换为人民币进行计算
            BigDecimal saleAmountCNY = convertToCNY(vo.getPlatformCurrency(), vo.getSalesAmount());
            BigDecimal returnCategoryCommissionCNY = convertToCNY(vo.getPlatformCurrency(), vo.getReturnCommissionAmount());
            BigDecimal cancelledAmountCNY = convertToCNY(vo.getPlatformCurrency(), vo.getCancelledAmount());
            BigDecimal categoryCommissionCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getCategoryCommissionCost());
            BigDecimal logisticsTransferCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getLogisticsTransferCost());
            BigDecimal logisticsLastMileCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getLogisticsLastMileCost());
            BigDecimal logisticsDropOffCNY = convertToCNY(vo.getPlatformCurrency(), vo.getLogisticsDropOff());
            BigDecimal orderFeeCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getOrderFeeCost());
            BigDecimal reverseLogisticsCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getReverseLogisticsCost());
            BigDecimal refundAmountCNY = convertToCNY(vo.getPlatformCurrency(), vo.getRefundAmount());
            BigDecimal adCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getAdCost());
            BigDecimal platformServiceCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getPlatformServiceCost());
            
            // 计算人民币结算金额
            BigDecimal totalSettAmountCNY = saleAmountCNY
                    .add(returnCategoryCommissionCNY)
                    .subtract(cancelledAmountCNY.abs())
                    .subtract(categoryCommissionCostCNY.abs())
                    .subtract(logisticsTransferCostCNY.abs())
                    .subtract(logisticsLastMileCostCNY.abs())
                    .subtract(logisticsDropOffCNY.abs())
                    .add(orderFeeCostCNY) // 有正数和负数所以需要加法
                    .subtract(reverseLogisticsCostCNY.abs())
                    .subtract(refundAmountCNY.abs())
                    .subtract(adCostCNY.abs())
                    .subtract(platformServiceCostCNY.abs());
            
            // 转换其他成本为人民币 - 使用VO中的币种字段
            BigDecimal fboDeliverCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFboDeliverCost());
            BigDecimal fboInspectionCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFboInspectionCost());
            BigDecimal fbsCheckInCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFbsCheckInCost());
            BigDecimal fbsOperatingCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFbsOperatingCost());
            BigDecimal fbsOtherCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFbsOtherCost());
            BigDecimal purchaseCostCNY = convertToCNY(vo.getPurchaseCurrency(), vo.getPurchaseCost());
            BigDecimal purchaseShippingCostCNY = convertToCNY(vo.getPurchaseCurrency(), vo.getPurchaseShippingCost());
            BigDecimal logisticsShippingCostCNY = convertToCNY(vo.getLogisticsCurrency(), vo.getLogisticsShippingCost());
            BigDecimal salesVatCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getSalesVatCost());
            BigDecimal vatCostCNY = convertToCNY(vo.getCustomsCurrency(), vo.getVatCost());
            BigDecimal customsCostCNY = convertToCNY(vo.getCustomsCurrency(), vo.getCustomsCost());

            // 毛利润=结算金额-海外仓费用/FBO费用-采购成本-采购运费-头程运费-销售税-进口VAT-关税
            BigDecimal profitAmount = totalSettAmountCNY
                    .subtract(fboDeliverCostCNY.abs())
                    .subtract(fboInspectionCostCNY.abs())
                    .subtract(fbsCheckInCostCNY.abs())
                    .subtract(fbsOperatingCostCNY.abs())
                    .subtract(fbsOtherCostCNY.abs())
                    .subtract(purchaseCostCNY.abs())
                    .subtract(purchaseShippingCostCNY.abs())
                    .subtract(logisticsShippingCostCNY.abs())
                    .subtract(salesVatCostCNY.abs())
                    .subtract(vatCostCNY.abs())
                    .subtract(customsCostCNY.abs());

            BigDecimal totalCost = purchaseCostCNY.add(logisticsShippingCostCNY);
            vo.setRoi(safeDivide(profitAmount, totalCost.abs()));
            vo.setProfitAmount(profitAmount);
            vo.setProfitRate(safeDivide(profitAmount, saleAmountCNY));
        });
    }

    public void handleSkuProfitReport(List<ProfitReportRespVO> records) {
        List<String> clientIds = convertList(records, ProfitReportRespVO::getClientId);
        Map<String, ClientSimpleInfoDTO> shopMappingDOMap = ozonShopMappingService.batchSimpleInfoByClientIds(clientIds);

        List<Long> productIds = convertList(records, ProfitReportRespVO::getProductId);
        Map<Long, ProductSimpleInfoVO> productSimpleInfoMap = productInfoService.batchQueryProductSimpleInfo(productIds);

        // 预先加载汇率信息，避免多次查询
        String rubCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB");
        String usdCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "USD");

        // 先加载汇率数据，避免每次计算时都查询
        ExchangeRatesDO rubExchangeRatesDO = exchangeRatesService.getExchangeRatesByBaseCurrency(Integer.valueOf(rubCurrencyCode));
        ExchangeRatesDO usdExchangeRatesDO = exchangeRatesService.getExchangeRatesByBaseCurrency(Integer.valueOf(usdCurrencyCode));
        if (rubCurrencyCode != null && rubExchangeRatesDO.getCustomRate() != null) {
            RUB_EXCHANGE_RATE = rubExchangeRatesDO.getCustomRate();
        }
        if (usdCurrencyCode != null && usdExchangeRatesDO.getCustomRate() != null) {
            USD_EXCHANGE_RATE = usdExchangeRatesDO.getCustomRate();
        }


        // 获取SKU维度的广告费用数据，按日期分组
        final Map<String, cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO> adMap;
        if (CollectionUtils.isNotEmpty(records)) {
            // 获取所有的platformSkuId
            List<String> platformSkuIds = records.stream()
                    .map(ProfitReportRespVO::getPlatformSkuId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            
            if (CollectionUtils.isNotEmpty(platformSkuIds)) {
                // 获取财务日期范围
                LocalDate minDate = records.stream()
                        .map(vo -> LocalDate.parse(vo.getFinanceDate()))
                        .min(LocalDate::compareTo)
                        .orElse(LocalDate.now());
                LocalDate maxDate = records.stream()
                        .map(vo -> LocalDate.parse(vo.getFinanceDate()))
                        .max(LocalDate::compareTo)
                        .orElse(LocalDate.now());
                
                // 根据签收订单的接单日期查询广告费用，按日期分组
                adMap = ozonAdCampaignsItemService.getAdCostByInProcessDateAndSkuGroupByDate(
                        clientIds, platformSkuIds, minDate, maxDate);
            } else {
                adMap = new HashMap<>();
            }
        } else {
            adMap = new HashMap<>();
        }

        records.forEach(vo -> {
            if (shopMappingDOMap.containsKey(vo.getClientId())) {
                ClientSimpleInfoDTO clientSimpleInfoDTO = shopMappingDOMap.get(vo.getClientId());
                vo.setShopName(clientSimpleInfoDTO.getShopName());
                vo.setPlatform(clientSimpleInfoDTO.getPlatform());
                vo.setPlatformName(clientSimpleInfoDTO.getPlatformName());
            }
            if (MapUtils.isNotEmpty(productSimpleInfoMap) && productSimpleInfoMap.containsKey(vo.getProductId())) {
                vo.setProductSimpleInfo(productSimpleInfoMap.get(vo.getProductId()));
            }
            
            // 确保币种字段有值
            if (vo.getPlatformCurrency() == null) {
                vo.setPlatformCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB")));
            }
            if (vo.getFbsCurrency() == null) {
                vo.setFbsCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY")));
            }
            if (vo.getPurchaseCurrency() == null) {
                vo.setPurchaseCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY")));
            }
            if (vo.getLogisticsCurrency() == null) {
                vo.setLogisticsCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY")));
            }
            if (vo.getCustomsCurrency() == null) {
                vo.setCustomsCurrency(Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "USD")));
            }
            
            // 根据签收订单的接单日期查询广告费用，使用日期分组的key
            String adKey = vo.getFinanceDate() + "_" + vo.getClientId() + "_" + vo.getPlatformSkuId();
            if (adMap.containsKey(adKey)) {
                cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO adData = adMap.get(adKey);
                BigDecimal adAmount = adData.getOrdersMoney() != null ? adData.getOrdersMoney() : BigDecimal.ZERO;
                Integer adOrders = adData.getOrders() != null ? adData.getOrders() : 0;
                BigDecimal adSpendCost = adData.getMoneySpent() != null ? adData.getMoneySpent() : BigDecimal.ZERO;
                
                vo.setAdOrders(adOrders);
                vo.setAdAmount(convertPrice(vo.getPlatformCurrency(), adAmount));
                vo.setAdCost(convertPrice(vo.getPlatformCurrency(), adSpendCost.negate()));
            } else {
                // 没有广告数据时设置为0
                vo.setAdCost(BigDecimal.ZERO);
                vo.setAdOrders(0);
                vo.setAdAmount(BigDecimal.ZERO);
            }
            
            vo.setPlatformServiceCost(BigDecimal.ZERO);
            vo.setAcos(safeDivide(vo.getAdCost().abs(), vo.getAdAmount().abs()));
            vo.setAcoas(safeDivide(vo.getAdCost().abs(), vo.getSalesAmount().abs()));

            // 重新计算结算金额，与门店维度保持一致
            // 结算金额=销售金额+退还佣金-取消金额-类目佣金-送货费-逆向物流-赔偿-广告花费-平台服务费
            BigDecimal totalSettAmount = vo.getSalesAmount()
                    .add(vo.getReturnCommissionAmount())
                    .subtract(vo.getCancelledAmount().abs())
                    .subtract(vo.getCategoryCommissionCost().abs())
                    .subtract(vo.getLogisticsTransferCost().abs())
                    .subtract(vo.getLogisticsLastMileCost().abs())
                    .subtract(vo.getLogisticsDropOff().abs())
                    .add(vo.getOrderFeeCost()) // 有正数和负数所以需要加法
                    .subtract(vo.getReverseLogisticsCost().abs())
                    .subtract(vo.getRefundAmount().abs())
                    .subtract(vo.getAdCost().abs())
                    .subtract(vo.getPlatformServiceCost().abs());
            vo.setSettleAmount(totalSettAmount);
            
            // 转换为人民币进行计算
            BigDecimal saleAmountCNY = convertToCNY(vo.getPlatformCurrency(), vo.getSalesAmount());
            BigDecimal returnCategoryCommissionCNY = convertToCNY(vo.getPlatformCurrency(), vo.getReturnCommissionAmount());
            BigDecimal cancelledAmountCNY = convertToCNY(vo.getPlatformCurrency(), vo.getCancelledAmount());
            BigDecimal categoryCommissionCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getCategoryCommissionCost());
            BigDecimal logisticsTransferCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getLogisticsTransferCost());
            BigDecimal logisticsLastMileCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getLogisticsLastMileCost());
            BigDecimal logisticsDropOffCNY = convertToCNY(vo.getPlatformCurrency(), vo.getLogisticsDropOff());
            BigDecimal orderFeeCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getOrderFeeCost());
            BigDecimal reverseLogisticsCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getReverseLogisticsCost());
            BigDecimal refundAmountCNY = convertToCNY(vo.getPlatformCurrency(), vo.getRefundAmount());
            BigDecimal adCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getAdCost());
            BigDecimal platformServiceCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getPlatformServiceCost());
            
            // 计算人民币结算金额
            BigDecimal totalSettAmountCNY = saleAmountCNY
                    .add(returnCategoryCommissionCNY)
                    .subtract(cancelledAmountCNY.abs())
                    .subtract(categoryCommissionCostCNY.abs())
                    .subtract(logisticsTransferCostCNY.abs())
                    .subtract(logisticsLastMileCostCNY.abs())
                    .subtract(logisticsDropOffCNY.abs())
                    .add(orderFeeCostCNY) // 有正数和负数所以需要加法
                    .subtract(reverseLogisticsCostCNY.abs())
                    .subtract(refundAmountCNY.abs())
                    .subtract(adCostCNY.abs())
                    .subtract(platformServiceCostCNY.abs());
            
            // 转换其他成本为人民币 - 使用VO中的币种字段
            BigDecimal fboDeliverCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFboDeliverCost());
            BigDecimal fboInspectionCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFboInspectionCost());
            BigDecimal fbsCheckInCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFbsCheckInCost());
            BigDecimal fbsOperatingCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFbsOperatingCost());
            BigDecimal fbsOtherCostCNY = convertToCNY(vo.getFbsCurrency(), vo.getFbsOtherCost());
            BigDecimal purchaseCostCNY = convertToCNY(vo.getPurchaseCurrency(), vo.getPurchaseCost());
            BigDecimal purchaseShippingCostCNY = convertToCNY(vo.getPurchaseCurrency(), vo.getPurchaseShippingCost());
            BigDecimal logisticsShippingCostCNY = convertToCNY(vo.getLogisticsCurrency(), vo.getLogisticsShippingCost());
            BigDecimal salesVatCostCNY = convertToCNY(vo.getPlatformCurrency(), vo.getSalesVatCost());
            BigDecimal vatCostCNY = convertToCNY(vo.getCustomsCurrency(), vo.getVatCost());
            BigDecimal customsCostCNY = convertToCNY(vo.getCustomsCurrency(), vo.getCustomsCost());

            // 毛利润=结算金额-海外仓费用/FBO费用-采购成本-采购运费-头程运费-销售税-进口VAT-关税
            BigDecimal profitAmount = totalSettAmountCNY
                    .subtract(fboDeliverCostCNY.abs())
                    .subtract(fboInspectionCostCNY.abs())
                    .subtract(fbsCheckInCostCNY.abs())
                    .subtract(fbsOperatingCostCNY.abs())
                    .subtract(fbsOtherCostCNY.abs())
                    .subtract(purchaseCostCNY.abs())
                    .subtract(purchaseShippingCostCNY.abs())
                    .subtract(logisticsShippingCostCNY.abs())
                    .subtract(salesVatCostCNY.abs())
                    .subtract(vatCostCNY.abs())
                    .subtract(customsCostCNY.abs());

            BigDecimal totalCost = purchaseCostCNY.add(logisticsShippingCostCNY);
            vo.setRoi(safeDivide(profitAmount, totalCost.abs()));
            vo.setProfitAmount(profitAmount);
            vo.setProfitRate(safeDivide(profitAmount, saleAmountCNY));
        });
    }

    // 通用的安全除法方法，防止除以0的情况和空指针异常
    private BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal convertPrice(Integer currency, BigDecimal originalPrice) {
        if (originalPrice == null) {
            return BigDecimal.ZERO;
        }
        
        return originalPrice;
    }
    
    /**
     * 转换货币到人民币，用于计算毛利润
     * @param currency 货币类型
     * @param originalPrice 原始价格
     * @return 转换后的人民币价格
     */
    private BigDecimal convertToCNY(Integer currency, BigDecimal originalPrice) {
        if (originalPrice == null) {
            return BigDecimal.ZERO;
        }
        
        switch (currency) {
            case 20:
                return originalPrice.multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP);
            case 30:
                return originalPrice.multiply(USD_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP);
            default:
                return originalPrice;
        }
    }
}
