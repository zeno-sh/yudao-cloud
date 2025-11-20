package cn.iocoder.yudao.module.dm.infrastructure.ozon.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportSaveReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.transaction.vo.OzonFinanceTransactionPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeDetailDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeServicesDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.fbs.FbsPricingMethodConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.*;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProfitComputeService;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdCampaignsService;
import cn.iocoder.yudao.module.dm.service.exchangerates.ExchangeRatesService;
import cn.iocoder.yudao.module.dm.service.logistics.FbsFeeServicesService;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.productcosts.ProductCostsService;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportService;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import cn.iocoder.yudao.module.dm.service.transaction.OzonFinanceTransactionService;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsWarehouseService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * @author: Zeno
 * @createTime: 2024/10/12 11:31
 */
@Service("ozonProfitComputeService")
@Slf4j
public class OzonProfitComputeServiceImpl implements ProfitComputeService {

    private static final Logger logger = LoggerFactory.getLogger(OzonProfitComputeServiceImpl.class);

    @Resource
    private ProductInfoService productInfoService;
    @Resource
    private OzonFinanceTransactionService financeTransactionService;
    @Resource
    private OzonAdCampaignsService ozonAdCampaignsService;
    @Resource
    private OzonOrderService ozonOrderService;
    @Resource
    private OzonProductOnlineService productOnlineService;
    @Resource
    private FbsWarehouseService fbsWarehouseService;
    @Resource
    private FbsFeeServicesService fbsFeeServicesService;
    @Resource
    private OzonShopMappingService shopMappingService;
    @Resource
    private ProductCostsService productCostsService;
    @Resource
    private ProfitReportService profitReportService;
    @Resource
    private ExchangeRatesService exchangeRatesService;
    @Resource
    private ProfitReportTaskLogService taskLogService;

    //卢布汇率
    private BigDecimal RUB_EXCHANGE_RATE = new BigDecimal("0.075");
    private BigDecimal USD_EXCHANGE_RATE = new BigDecimal("7.018");
    //千克
    private final BigDecimal KILOGRAM = new BigDecimal("1000");
    // 任务ID的MDC键
    private static final String MDC_TASK_ID_KEY = "taskId";

    @Override
    public void computeProfitReport(String date, List<String> clientIds) {
        if (CollectionUtils.isEmpty(clientIds)) {
            return;
        }

        // 获取当前任务ID
        String taskId = MDC.get(MDC_TASK_ID_KEY);

        // 先加载汇率数据，避免每次计算时都查询
        ExchangeRatesDO rubExchangeRatesDO = exchangeRatesService.getExchangeRatesByCurrencyCode("RUB");
        ExchangeRatesDO usdExchangeRatesDO = exchangeRatesService.getExchangeRatesByCurrencyCode("USD");
        if (rubExchangeRatesDO.getCustomRate() != null) {
            RUB_EXCHANGE_RATE = rubExchangeRatesDO.getCustomRate();
            logger.info("从数据库加载卢布汇率: {}", RUB_EXCHANGE_RATE);
        }
        if (usdExchangeRatesDO.getCustomRate() != null) {
            USD_EXCHANGE_RATE = usdExchangeRatesDO.getCustomRate();
            logger.info("从数据库加载美元汇率: {}", USD_EXCHANGE_RATE);
        }

        // 预先加载店铺映射，避免后续多次查询
        List<OzonShopMappingDO> ozonShopMappingList = shopMappingService.batchShopListByClientIds(clientIds);
        Map<String, OzonShopMappingDO> shopMappingDOMap = convertMap(ozonShopMappingList, OzonShopMappingDO::getClientId);
        List<OzonProductOnlineDO> allProductOnline = productOnlineService.getAllProductOnlineByClientIds(clientIds);
        Map<String, String> platformSkuIdToOfferIdMap = convertMap(allProductOnline, OzonProductOnlineDO::getPlatformSkuId, OzonProductOnlineDO::getOfferId);

        // 获取产品成本数据
        List<ProductCostDTO> productCostDTOList = getProductCost(clientIds, allProductOnline, date, date);
        if (CollectionUtils.isEmpty(productCostDTOList)) {
            return;
        }

        // 按clientId分组处理，减少重复计算
        List<ProfitReportSaveReqVO> profitReportSaveReqVOList = new ArrayList<>();
        Map<String, List<ProductCostDTO>> productCostByClientMap = convertMultiMap(
                productCostDTOList.stream()
                        .filter(productCostDTO -> StringUtils.isNotBlank(productCostDTO.getClientId()))
                        .collect(Collectors.toList()),
                ProductCostDTO::getClientId);

        productCostByClientMap.forEach((clientId, clientProductCostList) -> {
            List<ProfitReportSaveReqVO> saveReqVOS = getProductProfit(date, clientProductCostList, platformSkuIdToOfferIdMap)
                    .stream()
                    .peek(profitReportSaveReqVO -> {
                        profitReportSaveReqVO.setClientId(clientId);
                        if (shopMappingDOMap.containsKey(clientId)) {
                            profitReportSaveReqVO.setTenantId(shopMappingDOMap.get(clientId).getTenantId());
                        }
                    })
                    .collect(Collectors.toList());

            profitReportSaveReqVOList.addAll(saveReqVOS);
        });

        // 批量保存数据，减少数据库操作
        if (!profitReportSaveReqVOList.isEmpty()) {
            profitReportService.batchCreateProfitReport(profitReportSaveReqVOList);
        }
    }

    private List<ProductCostDTO> getProductCost(List<String> clientIds, List<OzonProductOnlineDO> allProductOnline, String beginDate, String endDate) {
        // 使用更有效的批量数据加载方式
        // 1.查询结算账单 - 一次性加载所有需要的数据
        List<OzonFinanceTransactionDO> financeTransactionList = getFinanceTransactionList2(clientIds, beginDate, endDate);
        if (financeTransactionList.isEmpty()) {
            return Collections.emptyList();
        }

        // 2.已经签收订单 - 使用批量查询
        List<OzonOrderDO> signedOrderList = getSignedOrderList2(financeTransactionList);

        // 记录订单数量信息到日志
        String logMsg = String.format("账单数量: %d, 签收订单数量: %d", financeTransactionList.size(), signedOrderList.size());
        logger.info(logMsg);

        // 获取当前任务ID并记录到任务日志表中
        String taskId = MDC.get(MDC_TASK_ID_KEY);
        if (StringUtils.isNotBlank(taskId)) {
            taskLogService.appendExecuteLog(taskId, logMsg);
        }

        // 3.查询产品信息 - 避免循环查询
        // 构建platformSkuId的映射
        Map<String, Long> platformSkuIdToDmProductIdMap = allProductOnline.stream()
                .filter(product -> product.getDmProductId() != null)
                .collect(Collectors.toMap(
                        OzonProductOnlineDO::getPlatformSkuId,
                        OzonProductOnlineDO::getDmProductId,
                        (existing, replacement) -> existing // 如有重复，保留第一个
                ));

        // 4.查询产品成本结构 - 一次性加载所有产品成本
        Map<String, ProductCostsDO> productCostsMap = getProductCostsMap(platformSkuIdToDmProductIdMap);

        List<ProductCostDTO> result = new ArrayList<>();

        // 5.计算签收成本 - 使用批量处理（只有当有签收订单时才计算）
        if (!signedOrderList.isEmpty()) {
            List<OzonOrderItemDO> signedOrderItemList = getSignedOrderItemList(signedOrderList);
            if (!signedOrderItemList.isEmpty()) {
                List<ProductCostDTO> productCostDTOList = calculateCost2(signedOrderList, signedOrderItemList, platformSkuIdToDmProductIdMap, financeTransactionList, productCostsMap);
                result.addAll(productCostDTOList);
            }
        }

        // 6.计算取消/退货 成本 - 使用批量处理（即使没有签收订单也要计算）
        List<ProductCostDTO> cancelledproductCostDTOList = calculateCancelledCost(financeTransactionList, productCostsMap);
        result.addAll(cancelledproductCostDTOList);

        // 7.计算收单 成本 - 使用批量处理（即使没有签收订单也要计算）
        List<ProductCostDTO> orderFeeCostDTOList = calculateOrderFeeCost(clientIds, financeTransactionList, platformSkuIdToDmProductIdMap);
        result.addAll(orderFeeCostDTOList);

        return result;
    }

    private List<ProfitReportSaveReqVO> getProductProfit(String date, List<ProductCostDTO> productCostDTOList, Map<String, String> platformSkuIdToOfferIdMap) {
        if (productCostDTOList.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProfitReportSaveReqVO> productProfitList = new ArrayList<>();


        Map<String, List<ProductCostDTO>> productCostMapping = new HashMap<>();
        Map<String, Long> platformSkuIdMapping = new HashMap<>();

        log.info("productCostDTOList={}", JSON.toJSONString(productCostDTOList));

        // 预过滤数据，避免在循环中多次判断
        for (ProductCostDTO dto : productCostDTOList) {
            if (dto.getProductId() != null && StringUtils.isNotBlank(dto.getPlatformSkuId())) {
                String platformSkuId = dto.getPlatformSkuId();

                // 构建映射
                platformSkuIdMapping.put(platformSkuId, dto.getProductId());

                // 添加到分组映射
                List<ProductCostDTO> group = productCostMapping.computeIfAbsent(platformSkuId, k -> new ArrayList<>());
                group.add(dto);
            }
        }

        // 预分配结果集大小，避免扩容
        productProfitList = new ArrayList<>(productCostMapping.size());

        // 处理每个分组
        for (Map.Entry<String, List<ProductCostDTO>> entry : productCostMapping.entrySet()) {
            String platformSkuId = entry.getKey();
            List<ProductCostDTO> items = entry.getValue();

            if (items.isEmpty()) {
                continue;
            }

            ProfitReportSaveReqVO reportSaveReqVO = new ProfitReportSaveReqVO();
            reportSaveReqVO.setFinanceDate(LocalDate.parse(date, DatePattern.NORM_DATE_FORMATTER));
            reportSaveReqVO.setPlatformSkuId(platformSkuId);
            reportSaveReqVO.setProductId(platformSkuIdMapping.get(platformSkuId));
            reportSaveReqVO.setOfferId(platformSkuIdToOfferIdMap.get(platformSkuId));

            // 使用单次遍历方式计算汇总值，代替多次stream操作
            calculateSummaryValues(items, reportSaveReqVO);

            productProfitList.add(reportSaveReqVO);
        }

        return productProfitList;
    }

    /**
     * 单次遍历计算汇总值，替代多次stream操作，提高性能
     */
    private void calculateSummaryValues(List<ProductCostDTO> items, ProfitReportSaveReqVO reportSaveReqVO) {
        int orders = 0;
        int salesVolume = 0;
        BigDecimal saleAmount = BigDecimal.ZERO;
        BigDecimal purchaseCost = BigDecimal.ZERO;
        BigDecimal purchaseShippingCost = BigDecimal.ZERO;
        BigDecimal logisticsShippingCost = BigDecimal.ZERO;
        BigDecimal vatCost = BigDecimal.ZERO;
        BigDecimal customsCost = BigDecimal.ZERO;
        BigDecimal declaredValueCost = BigDecimal.ZERO;
        BigDecimal checkInCost = BigDecimal.ZERO;
        BigDecimal operatingCost = BigDecimal.ZERO;
        BigDecimal otherCost = BigDecimal.ZERO;
        BigDecimal categoryCommissionCost = BigDecimal.ZERO;
        BigDecimal returnCategoryCommission = BigDecimal.ZERO;
        BigDecimal reverseLogisticsCost = BigDecimal.ZERO;
        BigDecimal cancelledAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal orderFeeCost = BigDecimal.ZERO;
        BigDecimal platformLogisticsLastMileCost = BigDecimal.ZERO;
        BigDecimal platformLogisticsTransferCost = BigDecimal.ZERO;
        BigDecimal dropOff = BigDecimal.ZERO;
        int refundOrders = 0;
        BigDecimal fboDeliveryCost = BigDecimal.ZERO;
        BigDecimal fboInspectionCost = BigDecimal.ZERO;

        // 用于存储所有成本项的币种信息
        Integer platformCurrency = Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB")); // 平台收入支出默认卢布
        Integer fbsCurrency = Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY"));  // FBS相关默认人民币
        Integer purchaseCurrency = Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY"));
        // 采购相关默认人民币
        Integer logisticsCurrency = Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY"));
        Integer customsCurrency = null;

        // 用于合并所有退货订单，避免重复计算
        Set<String> allCancelledOrders = new HashSet<>();
        
        // 用于收集销售税率，为重新计算销售VAT做准备
        BigDecimal salesTaxRate = null;

        // 单次遍历计算所有值，同时收集币种信息
        for (ProductCostDTO item : items) {
            orders += (item.getOrders() != null) ? item.getOrders() : 0;
            salesVolume += (item.getSalesVolume() != null) ? item.getSalesVolume() : 0;

            saleAmount = saleAmount.add(Optional.ofNullable(item.getSaleAmount()).orElse(BigDecimal.ZERO));
            purchaseCost = purchaseCost.add(Optional.ofNullable(item.getPurchaseCost()).orElse(BigDecimal.ZERO));
            purchaseShippingCost = purchaseShippingCost.add(Optional.ofNullable(item.getPurchaseShippingCost()).orElse(BigDecimal.ZERO));
            logisticsShippingCost = logisticsShippingCost.add(Optional.ofNullable(item.getLogisticsShippingCost()).orElse(BigDecimal.ZERO));
            vatCost = vatCost.add(Optional.ofNullable(item.getVatCost()).orElse(BigDecimal.ZERO));
            customsCost = customsCost.add(Optional.ofNullable(item.getCustomsCost()).orElse(BigDecimal.ZERO));
            declaredValueCost = declaredValueCost.add(Optional.ofNullable(item.getDeclaredValueCost()).orElse(BigDecimal.ZERO));
            checkInCost = checkInCost.add(Optional.ofNullable(item.getFbsCheckInCost()).orElse(BigDecimal.ZERO));
            operatingCost = operatingCost.add(Optional.ofNullable(item.getFbsOperatingCost()).orElse(BigDecimal.ZERO));
            otherCost = otherCost.add(Optional.ofNullable(item.getFbsOtherCost()).orElse(BigDecimal.ZERO));
            categoryCommissionCost = categoryCommissionCost.add(Optional.ofNullable(item.getCategoryCommissionCost()).orElse(BigDecimal.ZERO));
            returnCategoryCommission = returnCategoryCommission.add(Optional.ofNullable(item.getReturnCommissionAmount()).orElse(BigDecimal.ZERO));
            reverseLogisticsCost = reverseLogisticsCost.add(Optional.ofNullable(item.getReverseLogisticsCost()).orElse(BigDecimal.ZERO));
            cancelledAmount = cancelledAmount.add(Optional.ofNullable(item.getCancelledAmount()).orElse(BigDecimal.ZERO));
            refundAmount = refundAmount.add(Optional.ofNullable(item.getRefundAmount()).orElse(BigDecimal.ZERO));
            orderFeeCost = orderFeeCost.add(Optional.ofNullable(item.getOrderFeeCost()).orElse(BigDecimal.ZERO));
            platformLogisticsLastMileCost = platformLogisticsLastMileCost.add(Optional.ofNullable(item.getPlatformLogisticsLastMileCost()).orElse(BigDecimal.ZERO));
            platformLogisticsTransferCost = platformLogisticsTransferCost.add(Optional.ofNullable(item.getPlatformLogisticsTransferCost()).orElse(BigDecimal.ZERO));
            dropOff = dropOff.add(Optional.ofNullable(item.getPlatformLogisticsDropOffCost()).orElse(BigDecimal.ZERO));
            
            // 合并所有退货订单到一个集合中，自动去重
            if (item.getCancelledOrders() != null) {
                allCancelledOrders.addAll(item.getCancelledOrders());
            }
            
            fboDeliveryCost = fboDeliveryCost.add(Optional.ofNullable(item.getFboDeliveryCost()).orElse(BigDecimal.ZERO));
            fboInspectionCost = fboInspectionCost.add(Optional.ofNullable(item.getFboInspectionCost()).orElse(BigDecimal.ZERO));

            // 收集币种信息
            if (item.getPlatformCurrency() != null) {
                platformCurrency = item.getPlatformCurrency();
            }
            if (item.getFbsCurrency() != null) {
                fbsCurrency = item.getFbsCurrency();
            }
            if (item.getPurchaseCurrency() != null) {
                purchaseCurrency = item.getPurchaseCurrency();
            }
            if (item.getLogisticsCurrency() != null) {
                logisticsCurrency = item.getLogisticsCurrency();
            }
            if (item.getCustomsCurrency() != null) {
                customsCurrency = item.getCustomsCurrency();
            }
        }

        // 计算去重后的退货订单数量
        refundOrders = allCancelledOrders.size();

        // 设置汇总值
        reportSaveReqVO.setOrders(orders);
        reportSaveReqVO.setSalesVolume(salesVolume);
        reportSaveReqVO.setSalesAmount(saleAmount);
        reportSaveReqVO.setPurchaseCost(purchaseCost);
        reportSaveReqVO.setPurchaseShippingCost(purchaseShippingCost);
        reportSaveReqVO.setLogisticsShippingCost(logisticsShippingCost);
        reportSaveReqVO.setVatCost(vatCost);
        reportSaveReqVO.setCustomsCost(customsCost);
        reportSaveReqVO.setDeclaredValueCost(declaredValueCost);
        reportSaveReqVO.setFbsCheckInCost(checkInCost);
        reportSaveReqVO.setFbsOperatingCost(operatingCost);
        reportSaveReqVO.setFbsOtherCost(otherCost);
        reportSaveReqVO.setCategoryCommissionCost(categoryCommissionCost);
        reportSaveReqVO.setReturnCommissionAmount(returnCategoryCommission);
        reportSaveReqVO.setReverseLogisticsCost(reverseLogisticsCost);
        reportSaveReqVO.setCancelledAmount(cancelledAmount);
        reportSaveReqVO.setRefundAmount(refundAmount);
        reportSaveReqVO.setOrderFeeCost(orderFeeCost);
        reportSaveReqVO.setLogisticsLastMileCost(platformLogisticsLastMileCost);
        reportSaveReqVO.setLogisticsTransferCost(platformLogisticsTransferCost);
        reportSaveReqVO.setLogisticsDropOff(dropOff);
        reportSaveReqVO.setRefundOrders(refundOrders);
        reportSaveReqVO.setFboDeliverCost(fboDeliveryCost);
        reportSaveReqVO.setFboInspectionCost(fboInspectionCost);
        
        // 重新计算销售VAT：基于实际交付销售额（总销售额 - 取消/退货金额）
        BigDecimal actualDeliveredSaleAmount = saleAmount.subtract(cancelledAmount.abs());
        
        // 从第一个商品的成本信息中获取销售税率（假设同一个SKU的税率是一致的）
        if (!items.isEmpty() && reportSaveReqVO.getProductId() != null) {
            try {
                // 获取产品成本信息以获得销售税率
                List<ProductCostsDO> productCostsList = productCostsService.batchProductCostsListByProductIds(Arrays.asList(reportSaveReqVO.getProductId()));
                if (!productCostsList.isEmpty()) {
                    ProductCostsDO productCosts = productCostsList.get(0);
                    if (productCosts.getSalesTaxRate() != null) {
                        salesTaxRate = productCosts.getSalesTaxRate();
                    }
                }
            } catch (Exception e) {
                logger.warn("获取产品成本信息失败，使用默认销售税率: {}", e.getMessage());
            }
        }
        
        // 如果没有获取到销售税率，使用默认值20%
        if (salesTaxRate == null) {
            salesTaxRate = new BigDecimal("20");
            logger.debug("使用默认销售税率: {}%", salesTaxRate);
        }
        
        // 计算基于实际交付销售额的销售VAT
        BigDecimal salesVatCost = actualDeliveredSaleAmount
                .multiply(salesTaxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .negate(); // 成本为负数
        
        reportSaveReqVO.setSalesVatCost(salesVatCost);
        
        logger.info("销售VAT重新计算完成 - 总销售额: {}, 取消金额: {}, 实际交付销售额: {}, 销售税率: {}%, 销售VAT: {}", 
                saleAmount, cancelledAmount, actualDeliveredSaleAmount, salesTaxRate, salesVatCost);

        // 保持原有币种，不进行转换
        // 结算金额=销售金额+退还佣金-取消金额-类目佣金-送货费-逆向物流-赔偿
        // 注意：收单费用不应该包含在结算金额中，它是单独的成本项
        BigDecimal totalSettleAmount = saleAmount
                .add(returnCategoryCommission)
                .subtract(cancelledAmount.abs())
                .subtract(categoryCommissionCost.abs())
                .subtract(platformLogisticsLastMileCost.abs())
                .subtract(platformLogisticsTransferCost.abs())
                .subtract(dropOff.abs())
                .subtract(reverseLogisticsCost.abs())
                .subtract(refundAmount);
        reportSaveReqVO.setSettleAmount(totalSettleAmount);

        // 设置币种信息
        reportSaveReqVO.setPlatformCurrency(platformCurrency);
        reportSaveReqVO.setFbsCurrency(fbsCurrency);
        reportSaveReqVO.setPurchaseCurrency(purchaseCurrency);
        reportSaveReqVO.setLogisticsCurrency(logisticsCurrency);
        reportSaveReqVO.setCustomsCurrency(customsCurrency);
    }

    private List<ProductCostDTO> calculateOrderFeeCost(List<String> clientIds, List<OzonFinanceTransactionDO> financeTransactionList,
                                                       Map<String, Long> platformSkuIdToDmProductIdMap) {
        // 缓存字典值，避免重复调用
        String otherType = DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "其他");

        // 1. 过滤出"其他"类型的交易记录
        List<OzonFinanceTransactionDO> orderFeeTransactionDOList = financeTransactionList.stream()
                .filter(transaction -> otherType.equals(transaction.getType()))
                .collect(Collectors.toList());

        if (orderFeeTransactionDOList.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取收单费用的posting_number，这些对应订单表中的order_number
        List<String> orderNumbers = orderFeeTransactionDOList.stream()
                .map(OzonFinanceTransactionDO::getPostingNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (orderNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 根据order_number批量查询订单，然后查询订单项
        List<OzonOrderDO> orders = ozonOrderService.batchOrderListByOrderNumbers(clientIds, orderNumbers);
        
        if (orders.isEmpty()) {
            logger.warn("根据order_number未找到任何订单: {}", orderNumbers);
            return Collections.emptyList();
        }
        
        // 获取所有订单的posting_number
        List<String> postingNumbers = convertList(orders, OzonOrderDO::getPostingNumber);
        List<OzonOrderItemDO> allOrderItems = ozonOrderService.batchOrderItemListByPostingNumbers(
                clientIds.toArray(new String[0]), postingNumbers);

        // 4. 创建订单号到订单项的映射
        Map<String, List<OzonOrderItemDO>> orderNumberToItemsMap = convertMultiMap(
                allOrderItems, item -> {
                    // 从posting_number中提取order_number
                    String postingNumber = item.getPostingNumber();
                    if (postingNumber != null && postingNumber.contains("-")) {
                        String[] parts = postingNumber.split("-");
                        if (parts.length >= 2) {
                            return parts[0] + "-" + parts[1];
                        }
                    }
                    return postingNumber;
                });

        List<ProductCostDTO> orderFeeCostDTOList = new ArrayList<>();

        // 5. 处理收单费用
        for (OzonFinanceTransactionDO transaction : orderFeeTransactionDOList) {
            String orderNumber = transaction.getPostingNumber(); // 这里的posting_number实际是order_number
            if (orderNumber == null) {
                continue;
            }

            List<OzonOrderItemDO> orderItems = orderNumberToItemsMap.get(orderNumber);
            
            if (orderItems != null && !orderItems.isEmpty()) {
                // 如果一个订单有多个商品，需要按比例分配收单费用
                if (orderItems.size() == 1) {
                    // 单商品订单，直接分配
                    OzonOrderItemDO orderItem = orderItems.get(0);
                    String platformSkuId = orderItem.getPlatformSkuId();
                    Long dmProductId = platformSkuIdToDmProductIdMap.get(platformSkuId);
                    if (dmProductId == null) {
                        logProductNotMapped(platformSkuId);
                        continue;
                    }

                    ProductCostDTO productCostDTO = initCost();
                    productCostDTO.setClientId(transaction.getClientId());
                    productCostDTO.setProductId(dmProductId);
                    productCostDTO.setOrders(0);
                    productCostDTO.setPlatformSkuId(platformSkuId);
                    productCostDTO.setOrderFeeCost(transaction.getAmount());

                    orderFeeCostDTOList.add(productCostDTO);
                    
                    logger.info("收单费用匹配成功(单商品): {} -> {}, 金额: {}", orderNumber, platformSkuId, transaction.getAmount());
                } else {
                    // 多商品订单，按商品价值比例分配
                    BigDecimal totalOrderValue = orderItems.stream()
                            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    if (totalOrderValue.compareTo(BigDecimal.ZERO) > 0) {
                        for (OzonOrderItemDO orderItem : orderItems) {
                            String platformSkuId = orderItem.getPlatformSkuId();
                            Long dmProductId = platformSkuIdToDmProductIdMap.get(platformSkuId);
                            if (dmProductId == null) {
                                logProductNotMapped(platformSkuId);
                                continue;
                            }

                            // 计算该商品的价值占比
                            BigDecimal itemValue = orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()));
                            BigDecimal ratio = itemValue.divide(totalOrderValue, 4, RoundingMode.HALF_UP);
                            BigDecimal allocatedFee = transaction.getAmount().multiply(ratio);

                            ProductCostDTO productCostDTO = initCost();
                            productCostDTO.setClientId(transaction.getClientId());
                            productCostDTO.setProductId(dmProductId);
                            productCostDTO.setOrders(0);
                            productCostDTO.setPlatformSkuId(platformSkuId);
                            productCostDTO.setOrderFeeCost(allocatedFee);

                            orderFeeCostDTOList.add(productCostDTO);
                            
                            logger.info("收单费用匹配成功(多商品): {} -> {}, 比例: {}, 分配金额: {}", 
                                    orderNumber, platformSkuId, ratio, allocatedFee);
                        }
                    }
                }
            } else {
                // 找不到对应的订单项，记录日志但不进行均摊
                logOrderProductNotFound(orderNumber);
                logger.warn("收单费用无法匹配到订单: {}, 金额: {}, 将被忽略", orderNumber, transaction.getAmount());
            }
        }

        return orderFeeCostDTOList;
    }

    private List<ProductCostDTO> calculateCancelledCost(List<OzonFinanceTransactionDO> financeTransactionList, Map<String, ProductCostsDO> productCostsMap) {
        List<ProductCostDTO> cancelledProductCostDTOList = new ArrayList<>();

        // 缓存字典值，避免重复调用
        String returnOrCancelType = DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "退货和取消订单");
        String refundType = DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "退款");

        List<OzonFinanceTransactionDO> cancelledTransactionDOList = financeTransactionList.stream()
                .filter(transaction -> transaction.getType().equals(returnOrCancelType) || transaction.getType().equals(refundType))
                .collect(Collectors.toList());

        log.info("处理取消订单和退款：{}", JSON.toJSONString(cancelledTransactionDOList));

        List<String> cancelledPostingNumbers = convertList(cancelledTransactionDOList, OzonFinanceTransactionDO::getPostingNumber);
        List<OzonOrderItemDO> cancelledOrderItemDOList = ozonOrderService.batchOrderItemListByPostingNumbers(null, cancelledPostingNumbers);
        Map<String, OzonOrderItemDO> orderItemDOMap = convertMap(cancelledOrderItemDOList, OzonOrderItemDO::getPostingNumber);

        Set<String> platformSkuIds = convertSet(cancelledOrderItemDOList, OzonOrderItemDO::getPlatformSkuId);
        List<OzonProductOnlineDO> productOnlineList = productOnlineService.batchOzonProductOnlineBySkuIds(platformSkuIds);
        Map<String, Long> platformSkuIdMapping = convertMap(productOnlineList.stream().filter(ozonProductOnlineDO -> ozonProductOnlineDO.getDmProductId() != null).collect(Collectors.toList()),
                OzonProductOnlineDO::getPlatformSkuId, OzonProductOnlineDO::getDmProductId);

        // 按posting_number分组处理，避免重复创建ProductCostDTO
        Map<String, List<OzonFinanceTransactionDO>> transactionsByPostingNumber = 
            cancelledTransactionDOList.stream()
                .collect(Collectors.groupingBy(OzonFinanceTransactionDO::getPostingNumber));

        for (Map.Entry<String, List<OzonFinanceTransactionDO>> entry : transactionsByPostingNumber.entrySet()) {
            String postingNumber = entry.getKey();
            List<OzonFinanceTransactionDO> transactions = entry.getValue();
            
            OzonOrderItemDO ozonOrderItemDO = orderItemDOMap.get(postingNumber);
            String platformSkuId = null;
            Long dmProductId = null;
            String clientId = null;
            String offerId = null;
            
            if (ozonOrderItemDO != null) {
                // 如果找到了订单项，使用订单项的信息
                platformSkuId = ozonOrderItemDO.getPlatformSkuId();
                dmProductId = platformSkuIdMapping.get(platformSkuId);
                clientId = ozonOrderItemDO.getClientId();
                offerId = ozonOrderItemDO.getOfferId();
            } else {
                // 如果没有找到订单项，尝试从交易记录中获取客户端ID，并通过posting_number查找产品信息
                logOrderProductNotFound(postingNumber);
                
                // 从交易记录中获取客户端ID
                if (!transactions.isEmpty()) {
                    clientId = transactions.get(0).getClientId();
                }
                
                // 尝试通过posting_number的前缀查找相关的订单项
                // posting_number格式通常是 orderNumber-itemNumber-returnNumber
                String basePostingNumber = postingNumber;
                if (postingNumber.contains("-")) {
                    String[] parts = postingNumber.split("-");
                    if (parts.length >= 2) {
                        basePostingNumber = parts[0] + "-" + parts[1];
                        // 尝试查找基础posting_number对应的订单项
                        List<OzonOrderItemDO> relatedOrderItems = ozonOrderService.batchOrderItemListByPostingNumbers(null, Arrays.asList(basePostingNumber));
                        if (!relatedOrderItems.isEmpty()) {
                            OzonOrderItemDO relatedItem = relatedOrderItems.get(0);
                            platformSkuId = relatedItem.getPlatformSkuId();
                            dmProductId = platformSkuIdMapping.get(platformSkuId);
                            
                            // 如果在现有映射中找不到，需要重新查询产品映射
                            if (dmProductId == null && StringUtils.isNotBlank(platformSkuId)) {
                                List<OzonProductOnlineDO> newProductOnlineList = productOnlineService.batchOzonProductOnlineBySkuIds(Collections.singleton(platformSkuId));
                                if (!newProductOnlineList.isEmpty() && newProductOnlineList.get(0).getDmProductId() != null) {
                                    dmProductId = newProductOnlineList.get(0).getDmProductId();
                                    // 更新映射，避免重复查询
                                    platformSkuIdMapping.put(platformSkuId, dmProductId);
                                }
                            }
                            
                            if (clientId == null) {
                                clientId = relatedItem.getClientId();
                            }
                            offerId = relatedItem.getOfferId();
                            logger.info("通过基础posting_number {} 找到相关产品信息: platformSkuId={}, dmProductId={}", 
                                    basePostingNumber, platformSkuId, dmProductId);
                        }
                    }
                }
                
                // 如果仍然找不到产品信息，跳过这个记录
                if (dmProductId == null) {
                    logger.warn("无法找到posting_number {} 对应的产品信息，跳过处理", postingNumber);
                    continue;
                }
            }
            
            if (dmProductId == null) {
                logProductNotMapped(platformSkuId);
                continue;
            }

            // 为每个posting_number创建一个ProductCostDTO
            ProductCostDTO productCostDTO = initCost();
            productCostDTO.setProductId(dmProductId);
            productCostDTO.setClientId(clientId);
            productCostDTO.setPlatformSkuId(platformSkuId);
            productCostDTO.setOrders(0);

            // 处理该posting_number下的所有交易记录
            for (OzonFinanceTransactionDO ozonFinanceTransactionDO : transactions) {
                String transactionType = ozonFinanceTransactionDO.getType();
                if (transactionType.equals(refundType)) {
                    // 退款类型处理，保持原有币种
                    BigDecimal currentRefund = Optional.ofNullable(productCostDTO.getRefundAmount()).orElse(BigDecimal.ZERO);
                    productCostDTO.setRefundAmount(currentRefund.add(ozonFinanceTransactionDO.getAmount()));
                } else if (transactionType.equals(returnOrCancelType)) {
                    // 退货和取消订单或其他类型处理，保持原有币种
                    processCancelledOrderTransaction(ozonFinanceTransactionDO, productCostDTO);
                }
            }

            // 计算退货商品的基础成本减少（采购成本、头程费、关税、进口VAT等）
            // 退货商品不会产生实际损耗，需要减少相应的产品成本
            if (ozonOrderItemDO != null && productCostDTO.getCancelledAmount().compareTo(BigDecimal.ZERO) != 0) {
                ProductCostsDO productCostsDO = productCostsMap.get(offerId);
                if (productCostsDO != null) {
                    // 获取产品采购信息
                    Map<Long, ProductPurchaseDO> productPurchaseMap = productInfoService.batchProductPurchaseListByProductIds(new Long[]{dmProductId});
                    ProductPurchaseDO productPurchaseDO = productPurchaseMap.get(dmProductId);
                    if (productPurchaseDO != null) {
                        Integer returnQuantity = ozonOrderItemDO.getQuantity();
                        
                        // 像正常订单一样计算退货商品的基础成本，然后作为负数减少成本
                        calculateReturnedProductBasicCost(productPurchaseDO, productCostsDO, productCostDTO, returnQuantity);
                        
                        logger.info("计算退货商品成本减少 - 商品ID: {}, 退货数量: {}, 减少采购成本: {}, 减少头程费: {}, 减少关税: {}, 减少进口VAT: {}", 
                                dmProductId, returnQuantity, 
                                productCostDTO.getPurchaseCost(),
                                productCostDTO.getLogisticsShippingCost(),
                                productCostDTO.getCustomsCost(),
                                productCostDTO.getVatCost());
                    }
                }
            }

            cancelledProductCostDTOList.add(productCostDTO);
        }

        return cancelledProductCostDTOList;
    }

    private List<ProductCostDTO> calculateCost2(List<OzonOrderDO> signedOrderList, List<OzonOrderItemDO> signedOrderItemList,
                                                Map<String, Long> platformSkuIdToDmProductIdMap, List<OzonFinanceTransactionDO> financeTransactionList,
                                                Map<String, ProductCostsDO> productCostsMap) {

        List<ProductCostDTO> productCostDTOList = new ArrayList<>();

        // 将后续所有的查询动作提前完成，避免循环中进行IO操作
        Map<String, OzonOrderDO> orderMap = convertMap(signedOrderList, OzonOrderDO::getPostingNumber);
        // 按订单ID对orderItem进行分组，避免重复统计
        Map<String, List<OzonOrderItemDO>> postingNumberToOrderItemsMap = convertMultiMap(signedOrderItemList, OzonOrderItemDO::getPostingNumber);

        // 提前一次性加载所有产品采购信息
        Set<Long> allProductIds = new HashSet<>(platformSkuIdToDmProductIdMap.values());
        Map<Long, ProductPurchaseDO> purchaseMap = productInfoService.batchProductPurchaseListByProductIds(allProductIds.toArray(new Long[0]));

        // 提前加载所有FBS仓库相关信息
        Map<String, FbsWarehouseMappingDO> fbsWarehouseMapping = loadFbsWarehouseInfo(signedOrderList);
        Map<Long, List<FbsFeeServicesDO>> fbsFeeServicesMapping = loadFbsServiceInfo(fbsWarehouseMapping);
        List<FbsFeeDetailDO> fbsFeeDetailList = loadFbsFeeDetails(fbsFeeServicesMapping);

        // 预计算订单的总金额，避免重复计算
        Map<String, OrderAmountInfo> orderAmountInfoMap = calculateOrderAmounts(postingNumberToOrderItemsMap);

        // 预分配结果集大小，减少扩容开销
        productCostDTOList = new ArrayList<>(signedOrderItemList.size());

        // 遍历签收的订单，按订单处理，避免重复统计
        for (OzonOrderDO ozonOrderDO : signedOrderList) {
            String postingNumber = ozonOrderDO.getPostingNumber();
            String orderNumber = ozonOrderDO.getOrderNumber();
            List<OzonOrderItemDO> orderItems = postingNumberToOrderItemsMap.get(postingNumber);

            if (CollectionUtils.isEmpty(orderItems)) {
                continue;
            }

            // 获取订单的总金额和各订单项金额占比信息
            OrderAmountInfo amountInfo = orderAmountInfoMap.get(postingNumber);
            if (amountInfo == null || amountInfo.getOrderTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 预先处理：找出订单中金额最大的SKU，用于订单计数
            Map<String, BigDecimal> itemRatioMap = amountInfo.getItemRatioMap();
            String maxAmountSkuId = null;
            BigDecimal maxRatio = BigDecimal.ZERO;

            for (Map.Entry<String, BigDecimal> entry : itemRatioMap.entrySet()) {
                if (entry.getValue().compareTo(maxRatio) > 0) {
                    maxRatio = entry.getValue();
                    maxAmountSkuId = entry.getKey();
                }
            }

            // 处理每个订单项
            for (OzonOrderItemDO ozonOrderItemDO : orderItems) {
                String platformSkuId = ozonOrderItemDO.getPlatformSkuId();
                Long dmProductId = platformSkuIdToDmProductIdMap.get(platformSkuId);

                if (dmProductId == null) {
                    logProductNotMapped(platformSkuId);
                    continue;
                }

                ProductCostDTO productCostDTO = initCost();
                productCostDTO.setClientId(ozonOrderDO.getClientId());
                productCostDTO.setProductId(dmProductId);
                productCostDTO.setPlatformSkuId(platformSkuId);

                // 获取该订单项的分配比例
                BigDecimal allocationRatio = amountInfo.getItemRatioMap().getOrDefault(platformSkuId, BigDecimal.ONE);

                // 计算签收成本 - 根据分配比例分配订单级别费用
                calculatePlatformCostWithRatio(orderNumber, financeTransactionList, productCostsMap.get(platformSkuId), productCostDTO, allocationRatio);

                // 计算 FBS/FBO 成本 - 根据订单类型选择对应的计算方法
                if (ozonOrderDO.getOrderType().equals(20)) { // FBS
                    calculateFbsCost(dmProductId, orderMap, ozonOrderItemDO, purchaseMap, fbsWarehouseMapping, fbsFeeServicesMapping, fbsFeeDetailList, productCostDTO);
                } else if (ozonOrderDO.getOrderType().equals(10)) { // FBO
                    calculateFboCost(dmProductId, orderMap, ozonOrderItemDO, productCostsMap.get(platformSkuId), purchaseMap, productCostDTO);
                }

                // 计算采购、头程、海关
                calculateBasicCost(purchaseMap.get(dmProductId), productCostsMap.get(platformSkuId), productCostDTO);

                productCostDTO.setSalesVolume(ozonOrderItemDO.getQuantity());

                // 只在订单中金额最大的SKU项上计数订单数量
                if (platformSkuId.equals(maxAmountSkuId)) {
                    productCostDTO.setOrders(1);
                } else {
                    productCostDTO.setOrders(0);
                }

                productCostDTOList.add(productCostDTO);
            }
        }

        return productCostDTOList;
    }

    /**
     * 提前加载FBS仓库信息
     */
    private Map<String, FbsWarehouseMappingDO> loadFbsWarehouseInfo(List<OzonOrderDO> signedOrderList) {
        List<String> platformWarehouseIds = signedOrderList.stream()
                .filter(order -> StringUtils.isNotBlank(order.getDeliveryMethod()))
                .map(order -> {
                    String deliveryMethod = order.getDeliveryMethod();
                    JSONObject jsonObject = JSON.parseObject(deliveryMethod);
                    // FBS 的订单才会有值 FBO 没有
                    return String.valueOf(JSONPath.eval(jsonObject, "$.warehouse_id"));
                }).collect(Collectors.toList());

        return fbsWarehouseService.getFbsWarehouseMappingByPlatformWarehouseIds(platformWarehouseIds);
    }

    /**
     * 提前加载FBS服务信息
     */
    private Map<Long, List<FbsFeeServicesDO>> loadFbsServiceInfo(Map<String, FbsWarehouseMappingDO> fbsWarehouseMapping) {
        List<Long> warehouseIds = fbsWarehouseMapping.values().stream()
                .map(FbsWarehouseMappingDO::getWarehouseId)
                .collect(Collectors.toList());
        return fbsFeeServicesService.batchFbsFeeServices(warehouseIds);
    }

    /**
     * 提前加载FBS费用详情
     */
    private List<FbsFeeDetailDO> loadFbsFeeDetails(Map<Long, List<FbsFeeServicesDO>> fbsFeeServicesMapping) {
        List<Long> serviceIds = fbsFeeServicesMapping.values().stream()
                .flatMap(Collection::stream)
                .map(FbsFeeServicesDO::getId)
                .collect(Collectors.toList());
        return fbsFeeServicesService.batchFbsFeeDetailListByServiceIds(serviceIds);
    }

    /**
     * 计算订单金额信息，用于按比例分配费用
     */
    private Map<String, OrderAmountInfo> calculateOrderAmounts(Map<String, List<OzonOrderItemDO>> postingNumberToOrderItemsMap) {
        Map<String, OrderAmountInfo> result = new HashMap<>();

        for (Map.Entry<String, List<OzonOrderItemDO>> entry : postingNumberToOrderItemsMap.entrySet()) {
            String postingNumber = entry.getKey();
            List<OzonOrderItemDO> items = entry.getValue();

            if (CollectionUtils.isEmpty(items)) {
                continue;
            }

            OrderAmountInfo amountInfo = new OrderAmountInfo();
            BigDecimal orderTotalAmount = BigDecimal.ZERO;
            Map<String, BigDecimal> itemAmountMap = new HashMap<>();

            // 计算订单总金额
            for (OzonOrderItemDO item : items) {
                BigDecimal itemAmount = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                itemAmountMap.put(item.getPlatformSkuId(), itemAmount);
                orderTotalAmount = orderTotalAmount.add(itemAmount);
            }

            amountInfo.setOrderTotalAmount(orderTotalAmount);

            // 计算每个订单项的金额占比
            Map<String, BigDecimal> itemRatioMap = new HashMap<>();
            if (orderTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
                for (Map.Entry<String, BigDecimal> itemEntry : itemAmountMap.entrySet()) {
                    String platformSkuId = itemEntry.getKey();
                    BigDecimal itemAmount = itemEntry.getValue();
                    BigDecimal ratio = itemAmount.divide(orderTotalAmount, 4, RoundingMode.HALF_UP);
                    itemRatioMap.put(platformSkuId, ratio);
                }
            } else {
                // 如果订单总金额为0，则平均分配
                BigDecimal equalRatio = BigDecimal.ONE.divide(new BigDecimal(items.size()), 4, RoundingMode.HALF_UP);
                for (OzonOrderItemDO item : items) {
                    itemRatioMap.put(item.getPlatformSkuId(), equalRatio);
                }
            }

            amountInfo.setItemRatioMap(itemRatioMap);
            result.put(postingNumber, amountInfo);
        }

        return result;
    }

    /**
     * 订单金额信息，包含订单总金额和各订单项的金额占比
     */
    private static class OrderAmountInfo {
        private BigDecimal orderTotalAmount;
        private Map<String, BigDecimal> itemRatioMap;

        public BigDecimal getOrderTotalAmount() {
            return orderTotalAmount;
        }

        public void setOrderTotalAmount(BigDecimal orderTotalAmount) {
            this.orderTotalAmount = orderTotalAmount;
        }

        public Map<String, BigDecimal> getItemRatioMap() {
            return itemRatioMap;
        }

        public void setItemRatioMap(Map<String, BigDecimal> itemRatioMap) {
            this.itemRatioMap = itemRatioMap;
        }
    }

    /**
     * 计算销售、取消/退货 成本 - 带分配比例
     *
     * @param orderNumber            订单号
     * @param financeTransactionList 财务交易列表
     * @param productCostsDO         产品成本数据对象
     * @param productCostDTO         产品成本DTO
     * @param allocationRatio        分配比例
     */
    private void calculatePlatformCostWithRatio(String orderNumber, List<OzonFinanceTransactionDO> financeTransactionList,
                                                ProductCostsDO productCostsDO, ProductCostDTO productCostDTO, BigDecimal allocationRatio) {
        // 缓存字典值，避免重复调用
        String orderType = DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "订单");

        // 过滤出与当前订单号匹配的交易
        List<OzonFinanceTransactionDO> transationList = filterList(financeTransactionList,
                financeTransactionDO -> financeTransactionDO.getPostingNumber() != null
                        && financeTransactionDO.getPostingNumber().contains(orderNumber));

        List<String> postingNumbers = convertList(transationList, OzonFinanceTransactionDO::getPostingNumber);
        log.info("postingNumbers:{}", postingNumbers.toString());

        Integer platformCurrency = productCostsDO == null ? 20 : productCostsDO.getPlatformCurrency();

        for (OzonFinanceTransactionDO ozonFinanceTransactionDO : transationList) {
            String transactionType = ozonFinanceTransactionDO.getType();
            if (transactionType.equals(orderType)) {
                // 订单类型处理 - 根据分配比例
                processOrderTransactionWithRatio(ozonFinanceTransactionDO, platformCurrency, productCostDTO, allocationRatio);
            }
        }
    }

    /**
     * 处理订单交易 - 带分配比例
     */
    private void processOrderTransactionWithRatio(OzonFinanceTransactionDO ozonFinanceTransactionDO, Integer platformCurrency,
                                                  ProductCostDTO productCostDTO, BigDecimal allocationRatio) {
        // 保持原有币种，不进行转换，只按比例分配
        BigDecimal settleAmount = ozonFinanceTransactionDO.getAmount().multiply(allocationRatio);
        BigDecimal categoryCommissionCost = ozonFinanceTransactionDO.getSaleCommission().multiply(allocationRatio);
        BigDecimal saleAmount = ozonFinanceTransactionDO.getAccrualsForSale().multiply(allocationRatio);

        // 设置结果
        productCostDTO.setSettleAmount(settleAmount);
        productCostDTO.setCategoryCommissionCost(categoryCommissionCost);
        productCostDTO.setSaleAmount(saleAmount);

        // 记录日志，便于调试
        logger.info("订单处理 - 原始金额:订单处理 - 原始金额: {}, 分配比例: {}, 最终金额: {}",
                ozonFinanceTransactionDO.getAccrualsForSale(),
                allocationRatio,
                saleAmount);

        List<ServiceDTO> serviceDTOS = JSONArray.parseArray(ozonFinanceTransactionDO.getServices(), ServiceDTO.class);

        // 合并多个filter，减少stream的处理次数
        if (CollectionUtils.isNotEmpty(serviceDTOS)) {
            BigDecimal logisticsLastMileCost = BigDecimal.ZERO;
            BigDecimal logisticsTransferCost = BigDecimal.ZERO;
            BigDecimal dropOff = BigDecimal.ZERO;
            for (ServiceDTO serviceDTO : serviceDTOS) {
                if (serviceDTO.getName().equals("MarketplaceServiceItemDelivToCustomer")) {
                    logisticsLastMileCost = logisticsLastMileCost.add(serviceDTO.getPrice());
                }
                if (serviceDTO.getName().equals("MarketplaceServiceItemDropoffSC")) {
                    dropOff = dropOff.add(serviceDTO.getPrice());
                }
                if (serviceDTO.getName().equals("MarketplaceServiceItemDirectFlowLogistic")) {
                    logisticsTransferCost = logisticsTransferCost.add(serviceDTO.getPrice());
                }
            }
            // 按比例设置物流费用，保持原有币种
            productCostDTO.setPlatformLogisticsLastMileCost(logisticsLastMileCost.multiply(allocationRatio));
            productCostDTO.setPlatformLogisticsTransferCost(logisticsTransferCost.multiply(allocationRatio));
            productCostDTO.setPlatformLogisticsDropOffCost(dropOff.multiply(allocationRatio));
        }
    }


    /**
     * 计算销售、取消/退货 成本
     *
     * @param orderNumber
     * @param financeTransactionList
     * @param productCostsDO
     * @param productCostDTO
     */
    private void calculatePlatformCost(String orderNumber, List<OzonFinanceTransactionDO> financeTransactionList, ProductCostsDO productCostsDO, ProductCostDTO productCostDTO) {

        // 缓存字典值，避免重复调用
        String orderType = DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "订单");

//        // 过滤出与当前订单号匹配的交易
        List<OzonFinanceTransactionDO> transationList = filterList(financeTransactionList,
                financeTransactionDO -> financeTransactionDO.getPostingNumber() != null
                        && financeTransactionDO.getPostingNumber().contains(orderNumber));

        List<String> postingNumbers = convertList(transationList, OzonFinanceTransactionDO::getPostingNumber);
        log.info("postingNumbers:{}", postingNumbers.toString());

        Integer platformCurrency = productCostsDO == null ? 20 : productCostsDO.getPlatformCurrency();

        for (OzonFinanceTransactionDO ozonFinanceTransactionDO : transationList) {
            String transactionType = ozonFinanceTransactionDO.getType();
            if (transactionType.equals(orderType)) {
                // 订单类型处理
                processOrderTransaction(ozonFinanceTransactionDO, platformCurrency, productCostDTO);
            }
        }
    }

    /**
     * 计算 FBS 成本 不包含售后
     *
     * @param dmProductId
     * @param orderMap
     * @param orderItemDO
     * @param purchaseMap
     * @param fbsWarehouseMapping
     * @param fbsFeeServicesMapping
     * @param fbsFeeDetailList
     * @param productCostDTO
     */
    private void calculateFbsCost(Long dmProductId, Map<String, OzonOrderDO> orderMap, OzonOrderItemDO orderItemDO,
                                  Map<Long, ProductPurchaseDO> purchaseMap, Map<String, FbsWarehouseMappingDO> fbsWarehouseMapping,
                                  Map<Long, List<FbsFeeServicesDO>> fbsFeeServicesMapping,
                                  List<FbsFeeDetailDO> fbsFeeDetailList, ProductCostDTO productCostDTO) {

        String postingNumber = orderItemDO.getPostingNumber();
        OzonOrderDO ozonOrderDO = orderMap.get(postingNumber);
        if (ozonOrderDO == null) {
            addErrorMsg("订单未找到", productCostDTO);
            return;
        }

        if (!ozonOrderDO.getOrderType().equals(20)) {
            return;
        }

        String deliveryMethod = ozonOrderDO.getDeliveryMethod();
        // FBS 的订单才会有值 FBO 没有
        if (StringUtils.isEmpty(deliveryMethod)) {
            return;
        }

        JSONObject jsonObject = JSON.parseObject(deliveryMethod);
        String platformWarehouseId = String.valueOf(JSONPath.eval(jsonObject, "$.warehouse_id"));
        if (StringUtils.isEmpty(platformWarehouseId)) {
            return;
        }

        FbsWarehouseMappingDO fbsWarehouseMappingDO = fbsWarehouseMapping.get(platformWarehouseId);
        if (fbsWarehouseMappingDO == null) {
            addErrorMsg(String.format("海外仓未映射：%s", platformWarehouseId), productCostDTO);
            return;
        }

        List<FbsFeeServicesDO> fbsFeeServices = fbsFeeServicesMapping.get(fbsWarehouseMappingDO.getWarehouseId());
        if (CollectionUtils.isEmpty(fbsFeeServices)) {
            addErrorMsg("海外仓未配置费用", productCostDTO);
            return;
        }

        ProductPurchaseDO productPurchaseDO = purchaseMap.get(dmProductId);
        if (productPurchaseDO == null) {
            addErrorMsg("本地产品未配置采购信息", productCostDTO);
            return;
        }

        Map<Long, List<FbsFeeDetailDO>> fbsFeeDatailMap = convertMultiMap(fbsFeeDetailList, FbsFeeDetailDO::getServiceId);
        Map<Integer, List<Long>> serviceTagMapping2 = convertMultiMap(fbsFeeServices, FbsFeeServicesDO::getTag, FbsFeeServicesDO::getId);

        serviceTagMapping2.forEach((tag, serviceIds) -> {
            BigDecimal serviceCost = BigDecimal.ZERO;
            for (Long serviceId : serviceIds) {
                serviceCost = serviceCost.add(getFbsServiceCost(productPurchaseDO, fbsFeeDatailMap.get(serviceId)));
                if (serviceCost.compareTo(BigDecimal.ZERO) > 0) {
                    // 根据不同的标签分配费用
                    assignServiceCost(tag, serviceCost, productCostDTO);
                }
            }
        });
    }

    private void calculateFboCost(Long dmProductId, Map<String, OzonOrderDO> orderMap, OzonOrderItemDO orderItemDO,
                                  ProductCostsDO productCostsDO, Map<Long, ProductPurchaseDO> purchaseMap,
                                  ProductCostDTO productCostDTO) {

        if (productCostsDO == null) {
            return;
        }

        Integer fboCurrency = productCostsDO.getFboCurrency() == null ? 20 : productCostsDO.getFboCurrency();
        String postingNumber = orderItemDO.getPostingNumber();

        OzonOrderDO ozonOrderDO = orderMap.get(postingNumber);
        if (ozonOrderDO == null) {
            addErrorMsg("订单未找到", productCostDTO);
            return;
        }
        if (!ozonOrderDO.getOrderType().equals(10)) {
            return;
        }

        ProductPurchaseDO productPurchaseDO = purchaseMap.get(dmProductId);
        if (productPurchaseDO == null) {
            addErrorMsg("本地产品未配置采购信息", productCostDTO);
            return;
        }

        // FBO 这里需要转换成卢布，根据用户设置的币种
        productCostDTO.setFboDeliveryCost(convertPriceToRub(productCostsDO.getFboCurrency(),
                calculateCostByUnitType(productCostsDO.getFboCurrency(),
                        productCostsDO.getFboDeliveryCost(), productPurchaseDO)));

    }

    /**
     * 计算采购、头程、海关、税务成本
     *
     * @param productPurchaseDO
     * @param productCostsDO
     * @param productCostDTO
     */
    private void calculateBasicCost(ProductPurchaseDO productPurchaseDO, ProductCostsDO productCostsDO, ProductCostDTO productCostDTO) {

        if (null == productCostsDO) {
            return;
        }

        // 采购成本，保持原有币种
        productCostDTO.setPurchaseCost(productCostsDO.getPurchaseCost().negate());

        if (productCostsDO.getPurchaseShippingCost() == null
                || productCostsDO.getPurchaseShippingUnit() == null
                || productCostsDO.getPurchaseShippingCost().compareTo(BigDecimal.ZERO) == 0) {
            productCostDTO.setPurchaseShippingCost(BigDecimal.ZERO);
        } else {
            productCostDTO.setPurchaseShippingCost(calculateCostByUnitType(productCostsDO.getPurchaseShippingUnit(),
                    productCostsDO.getPurchaseShippingCost(), productPurchaseDO).negate());
        }

        // 头程
        if (productCostsDO.getLogisticsShippingCost() == null
                || productCostsDO.getLogisticsUnit() == null) {
            productCostDTO.setLogisticsShippingCost(BigDecimal.ZERO);
        } else {
            productCostDTO.setLogisticsShippingCost(calculateCostByUnitType(productCostsDO.getLogisticsUnit(),
                    productCostsDO.getLogisticsShippingCost(), productPurchaseDO).negate());
            productCostDTO.setLogisticsCurrency(productCostsDO.getLogisticsCurrency());
        }

        // ============海关===========
        // 申报货值
        if (productCostsDO.getCustomsDeclaredValue() != null) {
            productCostDTO.setDeclaredValueCost(productCostsDO.getCustomsDeclaredValue().negate());
            productCostDTO.setCustomsCurrency(productCostsDO.getCustomsCurrency());
        }

        // 关税金额=申报货值*关税税率
        BigDecimal customsCost = BigDecimal.ZERO;
        if (productCostsDO.getCustomsDuty() != null && productCostsDO.getCustomsDeclaredValue() != null) {
            customsCost = productCostsDO.getCustomsDeclaredValue()
                    .multiply(productCostsDO.getCustomsDuty().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            productCostDTO.setCustomsCost(customsCost.negate());
            productCostDTO.setCustomsCurrency(productCostsDO.getCustomsCurrency());
        }

        // 进口VAT=(申报货值+关税金额)*进口VAT税率
        if (productCostsDO.getImportVat() != null && productCostsDO.getCustomsDeclaredValue() != null) {
            // 使用原始申报货值，加上已计算的关税金额（取绝对值）
            BigDecimal vatBase = productCostsDO.getCustomsDeclaredValue().add(customsCost.abs());
            BigDecimal vatCost = vatBase.multiply(productCostsDO.getImportVat().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            productCostDTO.setVatCost(vatCost.negate());
            productCostDTO.setCustomsCurrency(productCostsDO.getCustomsCurrency());
        }

        // 注意：销售VAT现在在汇总阶段基于实际交付销售额统一计算，这里不再单独计算
        // 为了保持兼容性，这里仍然设置销售VAT，但会在汇总时被重新计算覆盖
        productCostDTO.setSalesVatCost(productCostDTO.getSaleAmount()
                .multiply(productCostsDO.getSalesTaxRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)).negate()
        );
    }
    
    /**
     * 计算退货商品的基础成本减少（采购成本、头程费、关税、进口VAT等）
     * 与calculateBasicCost逻辑相同，但结果为正数（表示减少成本）
     *
     * @param productPurchaseDO 产品采购信息
     * @param productCostsDO 产品成本配置
     * @param productCostDTO 产品成本DTO
     * @param returnQuantity 退货数量
     */
    private void calculateReturnedProductBasicCost(ProductPurchaseDO productPurchaseDO, ProductCostsDO productCostsDO, 
                                                   ProductCostDTO productCostDTO, Integer returnQuantity) {
        
        if (productCostsDO == null || returnQuantity == null || returnQuantity <= 0) {
            return;
        }

        // 采购成本减少 = 单位采购成本 * 退货数量，结果为正数（表示减少成本）
        BigDecimal purchaseCostReduction = productCostsDO.getPurchaseCost().multiply(new BigDecimal(returnQuantity));
        productCostDTO.setPurchaseCost(purchaseCostReduction);

        // 采购运费减少
        if (productCostsDO.getPurchaseShippingCost() != null
                && productCostsDO.getPurchaseShippingUnit() != null
                && productCostsDO.getPurchaseShippingCost().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal purchaseShippingCostReduction = calculateCostByUnitType(productCostsDO.getPurchaseShippingUnit(),
                    productCostsDO.getPurchaseShippingCost(), productPurchaseDO).multiply(new BigDecimal(returnQuantity));
            productCostDTO.setPurchaseShippingCost(purchaseShippingCostReduction);
        }

        // 头程费减少
        if (productCostsDO.getLogisticsShippingCost() != null
                && productCostsDO.getLogisticsUnit() != null) {
            BigDecimal logisticsShippingCostReduction = calculateCostByUnitType(productCostsDO.getLogisticsUnit(),
                    productCostsDO.getLogisticsShippingCost(), productPurchaseDO).multiply(new BigDecimal(returnQuantity));
            productCostDTO.setLogisticsShippingCost(logisticsShippingCostReduction);
            productCostDTO.setLogisticsCurrency(productCostsDO.getLogisticsCurrency());
        }

        // ============海关成本减少===========
        // 申报货值减少
        if (productCostsDO.getCustomsDeclaredValue() != null) {
            BigDecimal declaredValueCostReduction = productCostsDO.getCustomsDeclaredValue().multiply(new BigDecimal(returnQuantity));
            productCostDTO.setDeclaredValueCost(declaredValueCostReduction);
            productCostDTO.setCustomsCurrency(productCostsDO.getCustomsCurrency());
        }

        // 关税减少 = 申报货值 * 关税税率 * 退货数量
        BigDecimal customsCostReduction = BigDecimal.ZERO;
        if (productCostsDO.getCustomsDuty() != null && productCostsDO.getCustomsDeclaredValue() != null) {
            customsCostReduction = productCostsDO.getCustomsDeclaredValue()
                    .multiply(productCostsDO.getCustomsDuty().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .multiply(new BigDecimal(returnQuantity));
            productCostDTO.setCustomsCost(customsCostReduction);
            productCostDTO.setCustomsCurrency(productCostsDO.getCustomsCurrency());
        }

        // 进口VAT减少 = (申报货值 + 关税) * 进口VAT税率 * 退货数量
        if (productCostsDO.getImportVat() != null && productCostsDO.getCustomsDeclaredValue() != null) {
            // 使用单件商品的申报货值和关税
            BigDecimal singleItemDeclaredValue = productCostsDO.getCustomsDeclaredValue();
            BigDecimal singleItemCustomsCost = customsCostReduction.divide(new BigDecimal(returnQuantity), 4, RoundingMode.HALF_UP);
            BigDecimal vatBase = singleItemDeclaredValue.add(singleItemCustomsCost);
            BigDecimal vatCostReduction = vatBase.multiply(productCostsDO.getImportVat().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .multiply(new BigDecimal(returnQuantity));
            productCostDTO.setVatCost(vatCostReduction);
            productCostDTO.setCustomsCurrency(productCostsDO.getCustomsCurrency());
        }
        
        // 设置退货数量为负数（表示减少销量）
        productCostDTO.setSalesVolume(-returnQuantity);
    }

    private ProductCostDTO initCost() {
        ProductCostDTO productCostDTO = new ProductCostDTO();
        productCostDTO.setOrders(0);
        productCostDTO.setSaleAmount(BigDecimal.ZERO);
        productCostDTO.setSettleAmount(BigDecimal.ZERO);
        productCostDTO.setPurchaseCost(BigDecimal.ZERO);
        productCostDTO.setPurchaseShippingCost(BigDecimal.ZERO);
        productCostDTO.setLogisticsShippingCost(BigDecimal.ZERO);
        productCostDTO.setVatCost(BigDecimal.ZERO);
        productCostDTO.setCustomsCost(BigDecimal.ZERO);
        productCostDTO.setDeclaredValueCost(BigDecimal.ZERO);
        productCostDTO.setFbsCheckInCost(BigDecimal.ZERO);
        productCostDTO.setFbsOperatingCost(BigDecimal.ZERO);
        productCostDTO.setFbsOtherCost(BigDecimal.ZERO);
        productCostDTO.setCategoryCommissionCost(BigDecimal.ZERO);
        productCostDTO.setOrderFeeCost(BigDecimal.ZERO);
        productCostDTO.setPlatformLogisticsLastMileCost(BigDecimal.ZERO);
        productCostDTO.setPlatformLogisticsTransferCost(BigDecimal.ZERO);
        productCostDTO.setCancelledAmount(BigDecimal.ZERO);
        productCostDTO.setRefundAmount(BigDecimal.ZERO);
        productCostDTO.setAdCost(BigDecimal.ZERO);
        productCostDTO.setAdSaleAmount(BigDecimal.ZERO);
        productCostDTO.setAdOrders(0);
        productCostDTO.setPlatformServiceCost(BigDecimal.ZERO);
        productCostDTO.setFboDeliveryCost(BigDecimal.ZERO);
        productCostDTO.setFboInspectionCost(BigDecimal.ZERO);
        productCostDTO.setSalesVatCost(BigDecimal.ZERO);
        productCostDTO.setDamageCost(BigDecimal.ZERO);
        productCostDTO.setProfitAmount(BigDecimal.ZERO);
        productCostDTO.setReverseLogisticsCost(BigDecimal.ZERO);
        productCostDTO.setRefundSettleAmount(BigDecimal.ZERO);
        productCostDTO.setReturnCommissionAmount(BigDecimal.ZERO);
        productCostDTO.setCancelledOrders(new HashSet<>());

        return productCostDTO;
    }

    private BigDecimal calculateCostByUnitType(Integer unitType, BigDecimal unitPrice, ProductPurchaseDO productPurchaseDO) {
        switch (unitType) {
            case 10:
                BigDecimal grossWeight = calculateGrossWeight(productPurchaseDO);
                return grossWeight.multiply(unitPrice);
            case 20:
                BigDecimal volume = calculateVolume(productPurchaseDO);
                return volume.multiply(unitPrice);
            case 50:
                BigDecimal density = calculateDensity(calculateGrossWeight(productPurchaseDO), calculateVolume(productPurchaseDO));
                return density.multiply(unitPrice);
            default:
                return unitPrice;
        }
    }

    /**
     * 根据币种转换价格，FBO费用需要转换为卢布
     */
    private BigDecimal convertPriceToRub(Integer currency, BigDecimal originalPrice) {
        if (originalPrice == null) {
            return BigDecimal.ZERO;
        }
        
        // 根据币种转换为卢布
        String rubCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB");
        String usdCurrencyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "USD");
        String cnyCode = DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY");
        
        if (currency == null) {
            return originalPrice;
        }
        
        // 如果已经是卢布，直接返回
        if (currency.equals(Integer.valueOf(rubCurrencyCode))) {
            return originalPrice;
        }
        
        // 如果是美元，转换为卢布 (美元 -> 人民币 -> 卢布)
        if (currency.equals(Integer.valueOf(usdCurrencyCode))) {
            return originalPrice.multiply(USD_EXCHANGE_RATE).multiply(RUB_EXCHANGE_RATE);
        }
        
        // 如果是人民币，转换为卢布
        if (currency.equals(Integer.valueOf(cnyCode))) {
            return originalPrice.multiply(RUB_EXCHANGE_RATE);
        }
        
        // 其他币种暂时直接返回
        return originalPrice;
    }

    // 分配费用到ProductCostDTO
    private void assignServiceCost(Integer tag, BigDecimal serviceCost, ProductCostDTO productCostDTO) {
        if (tag != null) {
            switch (tag) {
                case 10:
                    productCostDTO.setFbsCheckInCost(serviceCost.negate());
                    break;
                case 20:
                    productCostDTO.setFbsOperatingCost(serviceCost.negate());
                    break;
                default:
                    break;
            }
        }
    }

    // 优化后的 getFbsServiceCost 方法
    private BigDecimal getFbsServiceCost(ProductPurchaseDO productPurchaseDO, List<FbsFeeDetailDO> fbsFeeDetailList) {
        BigDecimal cost = BigDecimal.ZERO;

        BigDecimal grossWeight = calculateGrossWeight(productPurchaseDO);
        BigDecimal volume = calculateVolume(productPurchaseDO);
        BigDecimal density = calculateDensity(grossWeight, volume);

        for (FbsFeeDetailDO fbsFeeDetailDO : fbsFeeDetailList) {
            BigDecimal serviceCost = calculateServiceCost(fbsFeeDetailDO, grossWeight, volume, density);
            cost = cost.add(serviceCost);
        }

        return cost;
    }

    // 单独的重量、体积、密度计算方法
    private BigDecimal calculateGrossWeight(ProductPurchaseDO productPurchaseDO) {
        return productPurchaseDO.getGrossWeight().divide(KILOGRAM, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVolume(ProductPurchaseDO productPurchaseDO) {
        BigDecimal lengthCM = productPurchaseDO.getLength();
        BigDecimal widthCM = productPurchaseDO.getWidth();
        BigDecimal heightCM = productPurchaseDO.getHeight();
        return lengthCM.multiply(widthCM).multiply(heightCM).divide(new BigDecimal("1000000"), 3, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDensity(BigDecimal grossWeight, BigDecimal volume) {
        if (volume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return grossWeight.divide(volume, 3, RoundingMode.HALF_UP);
    }

    // 单独的费用计算方法
    private BigDecimal calculateServiceCost(FbsFeeDetailDO fbsFeeDetailDO, BigDecimal grossWeight, BigDecimal volume, BigDecimal density) {
        Integer pricingMethod = fbsFeeDetailDO.getPricingMethod();
        Integer unitType = fbsFeeDetailDO.getUnit();
        BigDecimal price = BigDecimal.ZERO;
        BigDecimal unitPrice = fbsFeeDetailDO.getPrice();

        if (pricingMethod == FbsPricingMethodConfig.FIXED) {// 固定价格
            price = calculateUnitPrice(unitType, unitPrice, grossWeight, volume);
        } else if (FbsPricingMethodConfig.WEIGHT == pricingMethod) {
            if (isWithinRange(grossWeight, fbsFeeDetailDO.getMin(), fbsFeeDetailDO.getMax())) {
                price = calculateUnitPrice(unitType, unitPrice, grossWeight, volume);
            }
        } else if (FbsPricingMethodConfig.VOLUME == pricingMethod) {
            if (isWithinRange(volume, fbsFeeDetailDO.getMin(), fbsFeeDetailDO.getMax())) {
                price = calculateUnitPrice(unitType, unitPrice, grossWeight, volume);
            }
        } else if (FbsPricingMethodConfig.DENSITY == pricingMethod) {
            if (isWithinRange(density, fbsFeeDetailDO.getMin(), fbsFeeDetailDO.getMax())) {
                price = calculateUnitPrice(unitType, unitPrice, grossWeight, volume);
            }
        }

        return price;
    }

    private BigDecimal calculateUnitPrice(Integer unitType, BigDecimal unitPrice, BigDecimal grossWeight, BigDecimal volume) {
        switch (unitType) {
            case 10:
                // KG
                return unitPrice.multiply(grossWeight).setScale(2, RoundingMode.HALF_UP);
            case 20:
                // 立方
                return unitPrice.multiply(volume).setScale(2, RoundingMode.HALF_UP);
            default:
                return unitPrice;
        }
    }

    // 检查是否在 min 和 max 区间内
    private boolean isWithinRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }


    // 分离出订单类型的处理逻辑
    private void processOrderTransaction(OzonFinanceTransactionDO ozonFinanceTransactionDO, Integer platformCurrency, ProductCostDTO productCostDTO) {
        // 保持原有币种，不进行转换
        productCostDTO.setSettleAmount(ozonFinanceTransactionDO.getAmount());
        productCostDTO.setCategoryCommissionCost(ozonFinanceTransactionDO.getSaleCommission());
        productCostDTO.setSaleAmount(ozonFinanceTransactionDO.getAccrualsForSale());

        // 记录销售金额计算过程日志
        if (logger.isDebugEnabled()) {
            logger.debug("销售金额计算 - 订单号: {}, 原始金额: {}, 最终金额: {}",
                    ozonFinanceTransactionDO.getPostingNumber(),
                    ozonFinanceTransactionDO.getAccrualsForSale(),
                    productCostDTO.getSaleAmount());
        }

        List<ServiceDTO> serviceDTOS = JSONArray.parseArray(ozonFinanceTransactionDO.getServices(), ServiceDTO.class);

        // 合并多个filter，减少stream的处理次数
        if (CollectionUtils.isNotEmpty(serviceDTOS)) {
            BigDecimal logisticsLastMileCost = BigDecimal.ZERO;
            BigDecimal logisticsTransferCost = BigDecimal.ZERO;
            BigDecimal dropOff = BigDecimal.ZERO;
            for (ServiceDTO serviceDTO : serviceDTOS) {
                if (serviceDTO.getName().equals("MarketplaceServiceItemDelivToCustomer")) {
                    logisticsLastMileCost = logisticsLastMileCost.add(serviceDTO.getPrice());
                }
                if (serviceDTO.getName().equals("MarketplaceServiceItemDropoffSC")) {
                    dropOff = dropOff.add(serviceDTO.getPrice());
                }
                if (serviceDTO.getName().equals("MarketplaceServiceItemDirectFlowLogistic")) {
                    logisticsTransferCost = logisticsTransferCost.add(serviceDTO.getPrice());
                }
            }
            // 设置物流费用，保持原有币种
            productCostDTO.setPlatformLogisticsLastMileCost(logisticsLastMileCost);
            productCostDTO.setPlatformLogisticsTransferCost(logisticsTransferCost);
            productCostDTO.setPlatformLogisticsDropOffCost(dropOff);
        }
    }

    private void processCancelledOrderTransaction(OzonFinanceTransactionDO ozonFinanceTransactionDO, ProductCostDTO productCostDTO) {
        // 只有ClientReturnAgentOperation才统计为退货订单
        if (ozonFinanceTransactionDO.getOperationType().equals("ClientReturnAgentOperation")) {
            Set<String> cancelledOrders = productCostDTO.getCancelledOrders();
            if (StringUtils.isNotBlank(ozonFinanceTransactionDO.getPostingNumber())) {
                cancelledOrders.add(ozonFinanceTransactionDO.getPostingNumber());
                log.info("统计取消、退货的数量: {}, 操作类型: {}", ozonFinanceTransactionDO.getPostingNumber(), ozonFinanceTransactionDO.getOperationType());
            }
        }
        
        if (ozonFinanceTransactionDO.getOperationType().equals("OperationReturnGoodsFBSofRMS")) {
            // 逆向物流
            List<ServiceDTO> serviceDTOS = JSONArray.parseArray(ozonFinanceTransactionDO.getServices(), ServiceDTO.class);
            BigDecimal reverseLogisticsCost = BigDecimal.ZERO; // 逆向物流费
            BigDecimal returnLogisticsLastMile = BigDecimal.ZERO; // 最后一公里费用
            for (ServiceDTO serviceDTO : serviceDTOS) {
                if (serviceDTO.getName().equals("MarketplaceServiceItemReturnNotDelivToCustomer")
                        || serviceDTO.getName().equals("MarketplaceServiceItemReturnFlowLogistic") || serviceDTO.getName().equals("MarketplaceServiceItemRedistributionReturnsPVZ")) {
                    reverseLogisticsCost = reverseLogisticsCost.add(serviceDTO.getPrice());
                }
                if (serviceDTO.getName().equals("MarketplaceServiceItemDirectFlowLogistic")
                        || serviceDTO.getName().equals("MarketplaceServiceItemDropoffSC")) {
                    returnLogisticsLastMile = returnLogisticsLastMile.add(serviceDTO.getPrice());
                }
            }
            // 保持原有币种，不进行转换，累加而不是覆盖
            BigDecimal currentReverseLogistics = Optional.ofNullable(productCostDTO.getReverseLogisticsCost()).orElse(BigDecimal.ZERO);
            BigDecimal currentLastMile = Optional.ofNullable(productCostDTO.getPlatformLogisticsLastMileCost()).orElse(BigDecimal.ZERO);
            
            productCostDTO.setReverseLogisticsCost(currentReverseLogistics.add(reverseLogisticsCost));
            productCostDTO.setPlatformLogisticsLastMileCost(currentLastMile.add(returnLogisticsLastMile));
        } else if (ozonFinanceTransactionDO.getOperationType().equals("ClientReturnAgentOperation")) {
            // 客户退货，保持原有币种，累加而不是覆盖
            BigDecimal currentReturnCommission = Optional.ofNullable(productCostDTO.getReturnCommissionAmount()).orElse(BigDecimal.ZERO);
            BigDecimal currentCancelledAmount = Optional.ofNullable(productCostDTO.getCancelledAmount()).orElse(BigDecimal.ZERO);
            
            productCostDTO.setReturnCommissionAmount(currentReturnCommission.add(ozonFinanceTransactionDO.getSaleCommission()));
            productCostDTO.setCancelledAmount(currentCancelledAmount.add(ozonFinanceTransactionDO.getAccrualsForSale()));
        } else if (ozonFinanceTransactionDO.getOperationType().equals("OperationAgentStornoDeliveredToCustomer")) {
            // 客户取消，退还最后一公里费用
            List<ServiceDTO> serviceDTOS = JSONArray.parseArray(ozonFinanceTransactionDO.getServices(), ServiceDTO.class);
            BigDecimal returnLogisticsLastMile = BigDecimal.ZERO; // 取消后退还的最后一公里费用
            for (ServiceDTO serviceDTO : serviceDTOS) {
                if (serviceDTO.getName().equals("MarketplaceServiceItemDelivToCustomer")) {
                    returnLogisticsLastMile = returnLogisticsLastMile.add(serviceDTO.getPrice());
                }
            }
            // 保持原有币种，不进行转换，累加而不是覆盖
            BigDecimal currentLastMile = Optional.ofNullable(productCostDTO.getPlatformLogisticsLastMileCost()).orElse(BigDecimal.ZERO);
            productCostDTO.setPlatformLogisticsLastMileCost(currentLastMile.add(returnLogisticsLastMile));
        }
    }

    /**
     * 获取产品成本结构
     * key: platformSkuId
     *
     * @param platformSkuIdToDmProductIdMap
     * @return
     */
    private Map<String, ProductCostsDO> getProductCostsMap(Map<String, Long> platformSkuIdToDmProductIdMap) {

        // 获取所有产品ID的成本信息
        Collection<Long> dmProductIds = platformSkuIdToDmProductIdMap.values();
        List<ProductCostsDO> productCostsDOList = productCostsService.batchProductCostsListByProductIds(dmProductIds);

        Map<Long, ProductCostsDO> productCostsMap = convertMap(productCostsDOList, ProductCostsDO::getProductId);
        Map<Long, String> dmProductIdToPlatformSkuIdMap = MapUtils.invertMap(platformSkuIdToDmProductIdMap);

        // 创建最终以platformSkuId为key的Map
        Map<String, ProductCostsDO> platformSkuIdCostsMap = new HashMap<>();
        for (Map.Entry<Long, ProductCostsDO> entry : productCostsMap.entrySet()) {
            Long dmProductId = entry.getKey();
            ProductCostsDO productCosts = entry.getValue();
            String platformSkuId = dmProductIdToPlatformSkuIdMap.get(dmProductId);
            platformSkuIdCostsMap.put(platformSkuId, productCosts);
        }

        return platformSkuIdCostsMap;
    }

    private List<OzonOrderItemDO> getSignedOrderItemList(List<OzonOrderDO> signedOrderList) {
        if (CollectionUtils.isEmpty(signedOrderList)) {
            return Collections.emptyList();
        }
        List<String> postingNumbers = convertList(signedOrderList, OzonOrderDO::getPostingNumber);
        return ozonOrderService.batchOrderItemListByPostingNumbers(null, postingNumbers);
    }

    private void addErrorMsg(String message, ProductCostDTO productCostDTO) {
        List<String> errorMsg = productCostDTO.getErrorMsg();
        if (errorMsg == null) {
            errorMsg = new ArrayList<>();
            productCostDTO.setErrorMsg(errorMsg);
        }
        errorMsg.add(message);
    }

    private List<OzonFinanceTransactionDO> getFinanceTransactionList2(List<String> clientIds, String beginDate, String endDate) {

        List<OzonShopMappingDO> ozonShopMappingDOList = shopMappingService.batchShopListByClientIds(clientIds);
        List<String> ozonClientIds = ozonShopMappingDOList.stream()
                // 筛选出ozon平台 对应字典 dm_platform
                .filter(ozonShopMappingDO -> ozonShopMappingDO.getPlatform() == 10 || ozonShopMappingDO.getPlatform() == 20)
                .map(OzonShopMappingDO::getClientId)
                .collect(Collectors.toList());

        OzonFinanceTransactionPageReqVO reqVO = new OzonFinanceTransactionPageReqVO();
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        reqVO.setClientIds(ozonClientIds.toArray(new String[0]));

        LocalDate[] localDates = new LocalDate[2];
        localDates[0] = LocalDateTimeUtil.parseDate(beginDate, DatePattern.NORM_DATE_PATTERN);
        localDates[1] = LocalDateTimeUtil.parseDate(endDate, DatePattern.NORM_DATE_PATTERN);
        reqVO.setOperationDate(localDates);
        PageResult<OzonFinanceTransactionDO> pageResult = financeTransactionService.getOzonFinanceTransactionPage(reqVO);
        return pageResult.getList();
    }


    private List<OzonOrderDO> getSignedOrderList2(List<OzonFinanceTransactionDO> transactionList) {
        // 记录统计信息
        BigDecimal totalSaleAmount = BigDecimal.ZERO;
        List<OzonFinanceTransactionDO> orderTransactions = transactionList.stream()
                .filter(transaction -> transaction.getType().equals(DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "订单")))
                .collect(Collectors.toList());

        for (OzonFinanceTransactionDO transaction : orderTransactions) {
            totalSaleAmount = totalSaleAmount.add(transaction.getAccrualsForSale() != null ? transaction.getAccrualsForSale() : BigDecimal.ZERO);
        }

        logger.info("订单交易总数: {}, 原始销售金额总和(卢布): {}, 预计转换后金额(人民币): {}",
                orderTransactions.size(),
                totalSaleAmount,
                totalSaleAmount.multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP));

        List<String> signedPostingNumbers = orderTransactions.stream()
                .map(OzonFinanceTransactionDO::getPostingNumber)
                .collect(Collectors.toList());
        return ozonOrderService.batchOrderListByPostingNumbers(signedPostingNumbers);
    }

    private List<OzonOrderDO> getSignedOrderList(List<String> clientIds, String beginDate, String endDate) {
        List<OzonShopMappingDO> ozonShopMappingDOList = shopMappingService.batchShopListByClientIds(clientIds);
        List<String> ozonClientIds = ozonShopMappingDOList.stream()
                // 筛选出ozon平台 对应字典 dm_platform
                .filter(ozonShopMappingDO -> ozonShopMappingDO.getPlatform() == 10 || ozonShopMappingDO.getPlatform() == 20)
                .map(OzonShopMappingDO::getClientId)
                .collect(Collectors.toList());

        LocalDate[] localDates = new LocalDate[2];
        localDates[0] = LocalDateTimeUtil.parseDate(beginDate, DatePattern.NORM_DATE_PATTERN);
        localDates[1] = LocalDateTimeUtil.parseDate(endDate, DatePattern.NORM_DATE_PATTERN);
        List<OzonFinanceTransactionDO> sigendTransactionList = financeTransactionService.getSigendTransactionList(ozonClientIds, localDates);

        List<String> signedPostingNumbers = convertList(sigendTransactionList, OzonFinanceTransactionDO::getPostingNumber);
        return ozonOrderService.batchOrderListByPostingNumbers(signedPostingNumbers);
    }

    /**
     * 输出结构化的警告日志
     *
     * @param message 警告消息
     * @param args    参数
     */
    private void logStructuredWarning(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        logger.warn(message);
    }

    /**
     * 记录订单商品不存在的警告日志
     * 集中处理相似类型的日志，避免代码中出现重复的日志记录
     *
     * @param orderNumber 订单号
     */
    private void logOrderProductNotFound(String orderNumber) {
        logStructuredWarning("订单号：%s，不存在，无法计算收单成本", orderNumber);
    }

    /**
     * 记录商品未映射的警告日志
     *
     * @param skuId 商品ID
     */
    private void logProductNotMapped(String skuId) {
        logStructuredWarning("在线商品未映射：%s", skuId);
    }
}
