package cn.iocoder.yudao.module.dm.service.profitreport.v2.calculation;

import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeDetailDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeServicesDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.fbs.FbsPricingMethodConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ServiceDTO;
import cn.iocoder.yudao.module.dm.service.logistics.FbsFeeServicesService;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationConfig;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.DataCollectionResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.util.CalculationUtils;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.util.ProfitCalculationLogger;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsWarehouseService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

/**
 * 签收订单成本计算器
 * 基于技术文档的核心业务逻辑实现，正确使用CalculationConfig
 * 
 * 根据数据层复用设计方案，直接输出ProfitReportDO格式
 *
 * @author Jax
 */
@Component
@Slf4j
public class SignedOrderCostCalculator implements CostCalculator {

    @Resource
    private ProfitCalculationLogger profitLogger;
    
    @Resource
    private FbsWarehouseService fbsWarehouseService;
    
    @Resource
    private FbsFeeServicesService fbsFeeServicesService;

    @Override
    public CostCalculatorType getType() {
        return CostCalculatorType.SIGNED_ORDER;
    }
    
    @Override
    public boolean supports(DataCollectionResult dataResult) {
        return dataResult.getOrderData().getSignedOrders() != null 
               && !dataResult.getOrderData().getSignedOrders().isEmpty();
    }
    
    @Override
    public List<ProfitReportDO> calculate(
            DataCollectionResult dataResult, 
            CalculationConfig config, 
            String taskId) {
        
        log.info("开始计算签收订单成本: taskId={}, 使用精度={}, 舍入模式={}", 
                taskId, config.getPrecision(), config.getRoundingMode());
        
        List<ProfitReportDO> results = new ArrayList<>();
        
        try {
            // 获取基础数据
            List<OzonOrderDO> signedOrders = dataResult.getOrderData().getSignedOrders();
            List<OzonOrderItemDO> orderItems = dataResult.getOrderData().getSignedOrderItems();
            List<OzonFinanceTransactionDO> transactions = dataResult.getCostData().getFinanceTransactions();
            Map<Long, ProductCostsDO> productCostsMap = dataResult.getCostData().getProductCostsMap();
            Map<Long, ProductPurchaseDO> productPurchaseMap = dataResult.getProductData().getProductPurchases();
            
            // 按订单分组处理
            Map<String, List<OzonOrderItemDO>> orderItemsGrouped = orderItems.stream()
                    .collect(Collectors.groupingBy(OzonOrderItemDO::getPostingNumber));
            
            // 预计算订单金额分配（传入config）
            Map<String, OrderAmountInfo> orderAmountInfoMap = calculateOrderAmounts(orderItemsGrouped, config);
            
            // 遍历每个签收订单
            for (OzonOrderDO order : signedOrders) {
                String postingNumber = order.getPostingNumber();
                List<OzonOrderItemDO> items = orderItemsGrouped.get(postingNumber);
                
                if (items == null || items.isEmpty()) {
                    profitLogger.logStructuredError(taskId,"订单没有找到对应的商品明细: postingNumber=%s", postingNumber);
                    continue;
                }
                
                // 获取订单的金额分配信息
                OrderAmountInfo amountInfo = orderAmountInfoMap.get(postingNumber);
                String maxAmountSkuId = amountInfo.getMaxAmountSkuId();
                
                // 处理每个订单项
                for (OzonOrderItemDO item : items) {
                    String platformSkuId = item.getPlatformSkuId();
                    Long dmProductId = dataResult.getProductData().getPlatformSkuIdMapping().get(platformSkuId);
                    
                    if (dmProductId == null) {
                        profitLogger.logProductNotMapped(taskId, platformSkuId);
                        continue;
                    }
                    
                    // 直接创建ProfitReportDO
                    ProfitReportDO profitReport = createProfitReportDO(item, order, dmProductId, transactions);
                    
                    // 获取该商品的分配比例
                    BigDecimal allocationRatio = amountInfo.getItemRatioMap().getOrDefault(platformSkuId, BigDecimal.ONE);
                    
                    // 获取产品成本配置
                    ProductCostsDO productCosts = productCostsMap.get(dmProductId);
                    ProductPurchaseDO productPurchase = productPurchaseMap.get(dmProductId);
                    
                    // 对productPurchase进行null检查
                    if (productPurchase == null) {
                        profitLogger.logStructuredWarning(taskId, "产品采购信息未找到: dmProductId=%s, platformSkuId=%s", dmProductId, platformSkuId);
                    }
                    
                    // 设置币种信息（复用现有表的优秀设计）
                    setProfitReportCurrencies(profitReport, productCosts);
                    
                    // 1. 计算平台费用（销售金额、佣金、物流费等）
                    calculatePlatformCosts(profitReport, postingNumber, transactions, allocationRatio, config);
                    
                    // 2. 计算基础成本（采购、头程、海关等）
                    calculateBasicCosts(profitReport, productCosts, productPurchase, item.getQuantity(), config);
                    
                    // 3. 计算仓储费用（FBS/FBO）
                    calculateStorageCosts(profitReport, order, item, productCosts, productPurchase, config, taskId);
                    
                    // 4. 设置订单计数（只在金额最大的SKU上计数，且销售金额大于0时才计数）
                    if (platformSkuId.equals(maxAmountSkuId) && 
                        profitReport.getSalesAmount() != null && 
                        profitReport.getSalesAmount().compareTo(BigDecimal.ZERO) > 0) {
                        profitReport.setOrders(1);
                    } else {
                        profitReport.setOrders(0);
                    }

                    // 5. 设置销量统计（只有销售金额大于0时才统计销量）
                    if (profitReport.getSalesAmount() != null && 
                        profitReport.getSalesAmount().compareTo(BigDecimal.ZERO) > 0) {
                        profitReport.setSalesVolume(item.getQuantity());
                    } else {
                        profitReport.setSalesVolume(0);
                    }
                    
                    results.add(profitReport);
                }
            }
            
            log.info("签收订单成本计算完成: taskId={}, 处理订单数={}, 生成成本明细数={}", 
                    taskId, signedOrders.size(), results.size());
            
        } catch (Exception e) {
            log.error("签收订单成本计算失败: taskId={}", taskId, e);
            throw new RuntimeException("签收订单成本计算失败", e);
        }
        
        return results;
    }
    
    /**
     * 预计算订单金额分配信息
     * 现在正确使用CalculationConfig
     */
    private Map<String, OrderAmountInfo> calculateOrderAmounts(Map<String, List<OzonOrderItemDO>> orderItemsGrouped, CalculationConfig config) {
        Map<String, OrderAmountInfo> result = new HashMap<>();
        
        for (Map.Entry<String, List<OzonOrderItemDO>> entry : orderItemsGrouped.entrySet()) {
            String postingNumber = entry.getKey();
            List<OzonOrderItemDO> items = entry.getValue();
            
            // 计算订单总金额（使用CalculationUtils）
            BigDecimal totalOrderValue = items.stream()
                    .map(item -> CalculationUtils.multiply(item.getPrice(), new BigDecimal(item.getQuantity()), config))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 找到金额最大的SKU
            String maxAmountSkuId = items.stream()
                    .max(Comparator.comparing(item -> CalculationUtils.multiply(item.getPrice(), new BigDecimal(item.getQuantity()), config)))
                    .map(OzonOrderItemDO::getPlatformSkuId)
                    .orElse(null);
            
            // 计算每个商品的分配比例（使用CalculationUtils）
            Map<String, BigDecimal> itemRatioMap = new HashMap<>();
            for (OzonOrderItemDO item : items) {
                if (totalOrderValue.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal itemValue = CalculationUtils.multiply(item.getPrice(), new BigDecimal(item.getQuantity()), config);
                    BigDecimal ratio = CalculationUtils.divide(itemValue, totalOrderValue, config);
                    itemRatioMap.put(item.getPlatformSkuId(), ratio);
                } else {
                    itemRatioMap.put(item.getPlatformSkuId(), BigDecimal.ONE);
                }
            }
            
            OrderAmountInfo amountInfo = OrderAmountInfo.builder()
                    .postingNumber(postingNumber)
                    .totalAmount(totalOrderValue)
                    .maxAmountSkuId(maxAmountSkuId)
                    .itemRatioMap(itemRatioMap)
                    .build();
            
            result.put(postingNumber, amountInfo);
        }
        
        return result;
    }
    
    /**
     * 创建ProfitReportDO基础对象
     */
    private ProfitReportDO createProfitReportDO(OzonOrderItemDO item, OzonOrderDO order, Long dmProductId, 
                                              List<OzonFinanceTransactionDO> transactions) {
        // 获取财务日期：从相关的账单记录中获取 operationDate
        LocalDate financeDate = null;
        
        // 查找与当前订单相关的账单记录，优先获取 operationDate
        String postingNumber = order.getPostingNumber();
        List<OzonFinanceTransactionDO> orderTransactions = transactions.stream()
                .filter(t -> "orders".equals(t.getType()) && postingNumber.equals(t.getPostingNumber()))
                .collect(Collectors.toList());
        
        if (!orderTransactions.isEmpty()) {
            // 使用第一个账单记录的操作日期作为财务日期
            OzonFinanceTransactionDO firstTransaction = orderTransactions.get(0);
            if (firstTransaction.getOperationDate() != null) {
                financeDate = firstTransaction.getOperationDate();
            }
        }

        ProfitReportDO profitReport = new ProfitReportDO();
        profitReport.setClientId(order.getClientId());
        profitReport.setProductId(dmProductId);
        profitReport.setPlatformSkuId(item.getPlatformSkuId());
        profitReport.setOfferId(item.getOfferId());
        profitReport.setFinanceDate(financeDate);
        
        // 设置必要的默认值，避免数据库约束错误
        profitReport.setDeleted(false);
        profitReport.setTenantId(cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId());
        
        return profitReport;
    }
    
    /**
     * 设置ProfitReportDO的币种信息
     * 使用字典获取币种编码
     */
    private void setProfitReportCurrencies(ProfitReportDO profitReport, ProductCostsDO productCosts) {
        // 从字典获取币种编码
        Integer rubCurrency = Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB"));
        Integer cnyCode = Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "CNY"));
        
        // 平台币种默认为卢布
        profitReport.setPlatformCurrency(rubCurrency);
        
        if (productCosts != null) {
            // 直接使用产品成本配置中的币种，如果为空则使用默认值
            profitReport.setPurchaseCurrency(productCosts.getPurchaseCurrency() != null ? productCosts.getPurchaseCurrency() : cnyCode);
            profitReport.setLogisticsCurrency(productCosts.getLogisticsCurrency() != null ? productCosts.getLogisticsCurrency() : cnyCode);  
            profitReport.setCustomsCurrency(productCosts.getCustomsCurrency() != null ? productCosts.getCustomsCurrency() : cnyCode);
        } else {
            // 如果没有产品成本配置，使用默认币种
            profitReport.setPurchaseCurrency(cnyCode);
            profitReport.setLogisticsCurrency(cnyCode);
            profitReport.setCustomsCurrency(cnyCode);
        }
        
        // FBS币种默认为人民币
        profitReport.setFbsCurrency(cnyCode);
    }
    
    /**
     * 计算平台费用（基于账单表精确匹配）
     * 直接设置到ProfitReportDO
     */
    private void calculatePlatformCosts(ProfitReportDO profitReport, String postingNumber, 
                                      List<OzonFinanceTransactionDO> transactions, BigDecimal allocationRatio, CalculationConfig config) {
        
        // 筛选与当前订单相关的账单记录
        List<OzonFinanceTransactionDO> orderTransactions = transactions.stream()
                .filter(t -> "orders".equals(t.getType()) && postingNumber.equals(t.getPostingNumber()))
                .collect(Collectors.toList());
        
        for (OzonFinanceTransactionDO transaction : orderTransactions) {
            // 1. 销售金额（直接归属，按比例分配）
            if (transaction.getAccrualsForSale() != null) {
                BigDecimal salesAmount = CalculationUtils.multiply(transaction.getAccrualsForSale(), allocationRatio, config);
                profitReport.setSalesAmount(salesAmount);
            }
            
            // 2. 类目佣金（直接归属，按比例分配）
            if (transaction.getSaleCommission() != null) {
                BigDecimal commission = CalculationUtils.multiply(transaction.getSaleCommission(), allocationRatio, config);
                profitReport.setCategoryCommissionCost(commission);
            }
            
            // 3. 解析services字段获取物流费用
            if (transaction.getServices() != null) {
                parseAndSetLogisticsCosts(profitReport, transaction.getServices(), allocationRatio, config);
            }
        }
    }

    /**
     * 解析services字段并设置物流费用
     * 直接设置到ProfitReportDO
     */
    private void parseAndSetLogisticsCosts(ProfitReportDO profitReport, String servicesJson, BigDecimal allocationRatio, CalculationConfig config) {
        try {
            List<ServiceDTO> serviceDTOS = JSONArray.parseArray(servicesJson, ServiceDTO.class);

            BigDecimal logisticsLastMileCost = BigDecimal.ZERO;      // 最后一公里
            BigDecimal logisticsDropOff = BigDecimal.ZERO;           // DropOff
            BigDecimal logisticsTransferCost = BigDecimal.ZERO;      // 转运费
            BigDecimal otherAgentServiceCost = BigDecimal.ZERO;      // 其他代理服务费

            if (CollectionUtils.isNotEmpty(serviceDTOS)) {
                for (ServiceDTO serviceDTO : serviceDTOS) {
                    String serviceName = serviceDTO.getName();
                    BigDecimal allocatedPrice = CalculationUtils.multiply(serviceDTO.getPrice(), allocationRatio, config);
                    
                    if ("MarketplaceServiceItemDelivToCustomer".equals(serviceName)) {
                        // 最后一公里
                        logisticsLastMileCost = logisticsLastMileCost.add(allocatedPrice);
                    } else if ("MarketplaceServiceItemDropoffSC".equals(serviceName)) {
                        // DropOff
                        logisticsDropOff = logisticsDropOff.add(allocatedPrice);
                    } else if ("MarketplaceServiceItemDirectFlowLogistic".equals(serviceName)) {
                        // 转运费
                        logisticsTransferCost = logisticsTransferCost.add(allocatedPrice);
                    } else {
                        // 其他代理服务费
                        otherAgentServiceCost = otherAgentServiceCost.add(allocatedPrice);
                    }
                }
            }
            
            // 设置到ProfitReportDO
            profitReport.setLogisticsLastMileCost(logisticsLastMileCost);
            profitReport.setLogisticsDropOff(logisticsDropOff);
            profitReport.setLogisticsTransferCost(logisticsTransferCost);
            profitReport.setOtherAgentServiceCost(otherAgentServiceCost);
            
        } catch (Exception e) {
            log.error("解析services字段失败", e);
        }
    }
    
    /**
     * 计算基础成本（采购、头程、海关等）
     * 统一自统计成本为负数
     */
    private void calculateBasicCosts(ProfitReportDO profitReport, ProductCostsDO productCosts, 
                                   ProductPurchaseDO productPurchase, Integer quantity, CalculationConfig config) {
        
        if (productCosts == null || quantity == null || quantity <= 0) {
            return;
        }
        
        // 1. 采购成本
        if (productCosts.getPurchaseCost() != null) {
            BigDecimal purchaseCost = CalculationUtils.multiply(productCosts.getPurchaseCost(), new BigDecimal(quantity), config);
            profitReport.setPurchaseCost(purchaseCost.negate()); // 统一为负数
        }
        
        // 2. 采购运费 - 需要产品采购信息
        if (productCosts.getPurchaseShippingCost() != null && productPurchase != null) {
            BigDecimal shippingCost = calculateCostByUnitType(
                    productCosts.getPurchaseShippingUnit(),
                    productCosts.getPurchaseShippingCost(),
                    productPurchase,
                    quantity,
                    config
            );
            profitReport.setPurchaseShippingCost(shippingCost.negate()); // 统一为负数
        }
        
        // 3. 头程运费 - 需要产品采购信息
        if (productCosts.getLogisticsShippingCost() != null && productPurchase != null) {
            BigDecimal logisticsCost = calculateCostByUnitType(
                    productCosts.getLogisticsUnit(),
                    productCosts.getLogisticsShippingCost(),
                    productPurchase,
                    quantity,
                    config
            );
            profitReport.setLogisticsShippingCost(logisticsCost.negate()); // 统一为负数
        }
        
        // 4. 海关费用
        calculateCustomsCosts(profitReport, productCosts, quantity, config);
        
        // 5. 销售VAT - 根据销售金额和销售税率计算
        calculateSalesVat(profitReport, productCosts, config);
    }
    
    /**
     * 计算海关费用
     * 直接设置到ProfitReportDO
     * 统一海关费用为负数表示成本
     */
    private void calculateCustomsCosts(ProfitReportDO profitReport, ProductCostsDO productCosts, Integer quantity, CalculationConfig config) {
        
        // 申报货值
        if (productCosts.getCustomsDeclaredValue() != null) {
            BigDecimal declaredValue = CalculationUtils.multiply(productCosts.getCustomsDeclaredValue(), new BigDecimal(quantity), config);
            profitReport.setDeclaredValueCost(declaredValue);
        }
        
        // 关税金额 = 申报货值 * 关税税率
        BigDecimal customsCost = BigDecimal.ZERO;
        if (productCosts.getCustomsDuty() != null && productCosts.getCustomsDeclaredValue() != null) {
            BigDecimal dutyRate = CalculationUtils.percentageToDecimal(productCosts.getCustomsDuty(), config);
            BigDecimal declaredValueTotal = CalculationUtils.multiply(productCosts.getCustomsDeclaredValue(), new BigDecimal(quantity), config);
            customsCost = CalculationUtils.multiply(declaredValueTotal, dutyRate, config);
            profitReport.setCustomsCost(customsCost.negate()); // 统一为负数
        }
        
        // 进口VAT = (申报货值 + 关税金额) * 进口VAT税率
        if (productCosts.getImportVat() != null && productCosts.getCustomsDeclaredValue() != null) {
            BigDecimal declaredValueTotal = CalculationUtils.multiply(productCosts.getCustomsDeclaredValue(), new BigDecimal(quantity), config);
            BigDecimal vatBase = declaredValueTotal.add(customsCost.abs()); // 使用关税的绝对值计算基数
            BigDecimal vatRate = CalculationUtils.percentageToDecimal(productCosts.getImportVat(), config);
            BigDecimal vatCost = CalculationUtils.multiply(vatBase, vatRate, config);
            profitReport.setVatCost(vatCost.negate()); // 统一为负数
        }
    }
    
    /**
     * 计算销售VAT
     * 计算公式：销售金额 * 销售税率
     * 直接设置到ProfitReportDO
     */
    private void calculateSalesVat(ProfitReportDO profitReport, ProductCostsDO productCosts, CalculationConfig config) {
        
        if (productCosts == null || productCosts.getSalesTaxRate() == null || 
            profitReport.getSalesAmount() == null || profitReport.getSalesAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        // 销售VAT = 销售金额 * 销售税率
        BigDecimal salesTaxRate = productCosts.getSalesTaxRate();
        BigDecimal salesAmount = profitReport.getSalesAmount();
        BigDecimal salesTaxRateDecimal = CalculationUtils.percentageToDecimal(salesTaxRate, config);
        BigDecimal salesVatCost = CalculationUtils.multiply(salesAmount, salesTaxRateDecimal, config);
        
        // 设置为负数，表示成本
        profitReport.setSalesVatCost(salesVatCost.negate());
        
        log.debug("销售VAT计算完成: platformSkuId={}, 销售金额={}, 销售税率={}%, 销售VAT={}", 
                profitReport.getPlatformSkuId(), salesAmount, salesTaxRate, salesVatCost.negate());
    }
    
    /**
     * 计算仓储费用
     */
    private void calculateStorageCosts(ProfitReportDO profitReport, OzonOrderDO order, OzonOrderItemDO item,
                                     ProductCostsDO productCosts, ProductPurchaseDO productPurchase, CalculationConfig config, String taskId) {
        
        if (order.getOrderType() == null) {
            return;
        }
        
        if (order.getOrderType().equals(20)) { // FBS
            calculateFbsCosts(profitReport, order, item, productPurchase, config, taskId);
        } else if (order.getOrderType().equals(10)) { // FBO
            calculateFboCosts(profitReport, productCosts, productPurchase, item.getQuantity(), config);
        }
    }
    
    /**
     * 计算FBS仓储费用
     * 统一自统计成本为负数
     */
    private void calculateFbsCosts(ProfitReportDO profitReport, OzonOrderDO order, OzonOrderItemDO item,
                                 ProductPurchaseDO productPurchase, CalculationConfig config, String taskId) {
        
        if (order == null || !order.getOrderType().equals(20)) {
            return;
        }
        
        // 解析仓库ID
        String deliveryMethod = order.getDeliveryMethod();
        if (StringUtils.isEmpty(deliveryMethod)) {
            profitLogger.logStructuredWarning(taskId, "FBS订单没有配送方式信息: postingNumber=%s", order.getPostingNumber());
            return;
        }
        
        String platformWarehouseId = null;
        try {
            JSONObject jsonObject = JSON.parseObject(deliveryMethod);
            platformWarehouseId = String.valueOf(JSONPath.eval(jsonObject, "$.warehouse_id"));
        } catch (Exception e) {
            profitLogger.logStructuredError(taskId, "解析配送方式失败: postingNumber=%s, deliveryMethod=%s, error=%s", 
                    order.getPostingNumber(), deliveryMethod, e.getMessage());
            return;
        }
        
        if (StringUtils.isEmpty(platformWarehouseId) || "null".equals(platformWarehouseId)) {
            profitLogger.logStructuredWarning(taskId, "FBS订单没有仓库ID: postingNumber=%s", order.getPostingNumber());
            return;
        }
        
        // 获取仓库映射
        Map<String, FbsWarehouseMappingDO> fbsWarehouseMappings = fbsWarehouseService.getFbsWarehouseMappingByPlatformWarehouseIds(
                Collections.singletonList(platformWarehouseId));
        
        if (MapUtils.isEmpty(fbsWarehouseMappings)) {
            profitLogger.logStructuredWarning(taskId, "海外仓未映射: platformWarehouseId=%s, postingNumber=%s", 
                    platformWarehouseId, order.getPostingNumber());
            return;
        }
        
        FbsWarehouseMappingDO fbsWarehouseMapping = fbsWarehouseMappings.get(platformWarehouseId);
        if (fbsWarehouseMapping == null) {
            profitLogger.logStructuredWarning(taskId, "海外仓未映射: platformWarehouseId=%s, postingNumber=%s", 
                    platformWarehouseId, order.getPostingNumber());
            return;
        }
        
        // 获取费用服务配置
        Map<Long, List<FbsFeeServicesDO>> fbsFeeServicesMap = fbsFeeServicesService.batchFbsFeeServices(
                Collections.singletonList(fbsWarehouseMapping.getWarehouseId()));
        
        List<FbsFeeServicesDO> fbsFeeServices = fbsFeeServicesMap.get(fbsWarehouseMapping.getWarehouseId());
        if (CollectionUtils.isEmpty(fbsFeeServices)) {
            profitLogger.logStructuredWarning(taskId, "海外仓未配置费用: warehouseId=%s, postingNumber=%s", 
                    fbsWarehouseMapping.getWarehouseId(), order.getPostingNumber());
            return;
        }
        
        if (productPurchase == null) {
            profitLogger.logStructuredWarning(taskId, "产品采购信息未找到: platformSkuId=%s, postingNumber=%s", 
                    item.getPlatformSkuId(), order.getPostingNumber());
            return;
        }
        
        // 获取费用详情
        List<Long> serviceIds = convertList(fbsFeeServices, FbsFeeServicesDO::getId);
        List<FbsFeeDetailDO> fbsFeeDetails = fbsFeeServicesService.batchFbsFeeDetailListByServiceIds(serviceIds);
        
        if (CollectionUtils.isEmpty(fbsFeeDetails)) {
            profitLogger.logStructuredWarning(taskId, "海外仓费用详情未配置: serviceIds=%s, postingNumber=%s", 
                    serviceIds, order.getPostingNumber());
            return;
        }
        
        // 按服务ID分组费用详情
        Map<Long, List<FbsFeeDetailDO>> fbsFeeDetailMap = convertMultiMap(fbsFeeDetails, FbsFeeDetailDO::getServiceId);
        
        // 按标签分组服务
        Map<Integer, List<Long>> serviceTagMapping = convertMultiMap(fbsFeeServices, FbsFeeServicesDO::getTag, FbsFeeServicesDO::getId);
        
        // 计算各类费用
        serviceTagMapping.forEach((tag, serviceIdList) -> {
            BigDecimal totalServiceCost = BigDecimal.ZERO;
            
            for (Long serviceId : serviceIdList) {
                List<FbsFeeDetailDO> feeDetails = fbsFeeDetailMap.get(serviceId);
                if (CollectionUtils.isNotEmpty(feeDetails)) {
                    BigDecimal serviceCost = calculateFbsServiceCost(productPurchase, feeDetails, config);
                    totalServiceCost = CalculationUtils.add(totalServiceCost, serviceCost, config);
                }
            }
            
            if (totalServiceCost.compareTo(BigDecimal.ZERO) > 0) {
                // 根据标签分配费用，统一为负数
                assignServiceCost(tag, totalServiceCost.negate(), profitReport);
            }
        });
        
        log.debug("FBS费用计算完成: postingNumber={}, platformSkuId={}, warehouseId={}", 
                order.getPostingNumber(), item.getPlatformSkuId(), fbsWarehouseMapping.getWarehouseId());
    }
    
    /**
     * 计算FBS服务费用
     */
    private BigDecimal calculateFbsServiceCost(ProductPurchaseDO productPurchase, List<FbsFeeDetailDO> fbsFeeDetails, CalculationConfig config) {
        BigDecimal totalCost = BigDecimal.ZERO;
        
        // 计算产品的重量、体积、密度
        BigDecimal grossWeight = calculateGrossWeight(productPurchase, config);
        BigDecimal volume = calculateVolume(productPurchase, config);
        BigDecimal density = calculateDensity(grossWeight, volume, config);
        
        for (FbsFeeDetailDO feeDetail : fbsFeeDetails) {
            BigDecimal serviceCost = calculateServiceCost(feeDetail, grossWeight, volume, density, config);
            totalCost = CalculationUtils.add(totalCost, serviceCost, config);
        }
        
        return totalCost;
    }
    
    /**
     * 计算毛重量（千克）
     */
    private BigDecimal calculateGrossWeight(ProductPurchaseDO productPurchase, CalculationConfig config) {
        if (productPurchase.getGrossWeight() == null) {
            return BigDecimal.ZERO;
        }
        // 从克转换为千克
        return CalculationUtils.divide(productPurchase.getGrossWeight(), new BigDecimal("1000"), config);
    }
    
    /**
     * 计算体积（立方米）
     * 现在正确使用CalculationConfig
     */
    private BigDecimal calculateVolume(ProductPurchaseDO productPurchase, CalculationConfig config) {
        if (productPurchase == null) {
            return BigDecimal.ZERO;
        }
        
        if (productPurchase.getLength() != null && productPurchase.getWidth() != null && productPurchase.getHeight() != null) {
            BigDecimal lengthInM = CalculationUtils.multiply(productPurchase.getLength(), productPurchase.getWidth(), config);
            lengthInM = CalculationUtils.multiply(lengthInM, productPurchase.getHeight(), config);
            return CalculationUtils.divide(lengthInM, new BigDecimal("1000000"), config); // 转换为立方米
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 计算密度（千克/立方米）
     */
    private BigDecimal calculateDensity(BigDecimal grossWeight, BigDecimal volume, CalculationConfig config) {
        if (volume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return CalculationUtils.divide(grossWeight, volume, config);
    }
    
    /**
     * 计算单项服务费用
     */
    private BigDecimal calculateServiceCost(FbsFeeDetailDO feeDetail, BigDecimal grossWeight, BigDecimal volume, BigDecimal density, CalculationConfig config) {
        Integer pricingMethod = feeDetail.getPricingMethod();
        BigDecimal unitPrice = feeDetail.getPrice();
        
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal price = BigDecimal.ZERO;
        
        if (pricingMethod == FbsPricingMethodConfig.FIXED) {
            // 固定价格
            price = calculateUnitPrice(feeDetail.getUnit(), unitPrice, grossWeight, volume, config);
        } else if (pricingMethod == FbsPricingMethodConfig.WEIGHT) {
            // 按重量计费
            if (isWithinRange(grossWeight, feeDetail.getMin(), feeDetail.getMax())) {
                price = calculateUnitPrice(feeDetail.getUnit(), unitPrice, grossWeight, volume, config);
            }
        } else if (pricingMethod == FbsPricingMethodConfig.VOLUME) {
            // 按体积计费
            if (isWithinRange(volume, feeDetail.getMin(), feeDetail.getMax())) {
                price = calculateUnitPrice(feeDetail.getUnit(), unitPrice, grossWeight, volume, config);
            }
        } else if (pricingMethod == FbsPricingMethodConfig.DENSITY) {
            // 按密度计费
            if (isWithinRange(density, feeDetail.getMin(), feeDetail.getMax())) {
                price = calculateUnitPrice(feeDetail.getUnit(), unitPrice, grossWeight, volume, config);
            }
        }
        
        return price;
    }
    
    /**
     * 计算单价费用
     */
    private BigDecimal calculateUnitPrice(Integer unitType, BigDecimal unitPrice, BigDecimal grossWeight, BigDecimal volume, CalculationConfig config) {
        if (unitType == null) {
            return unitPrice;
        }
        
        switch (unitType) {
            case 10:
                // 按重量（千克）
                return CalculationUtils.multiply(unitPrice, grossWeight, config);
            case 20:
                // 按体积（立方米）
                return CalculationUtils.multiply(unitPrice, volume, config);
            default:
                return unitPrice;
        }
    }
    
    /**
     * 检查是否在指定区间内
     */
    private boolean isWithinRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (min == null && max == null) {
            return true;
        }
        if (min == null) {
            return value.compareTo(max) <= 0;
        }
        if (max == null) {
            return value.compareTo(min) >= 0;
        }
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }
    
    /**
     * 根据标签分配费用
     */
    private void assignServiceCost(Integer tag, BigDecimal serviceCost, ProfitReportDO profitReport) {
        if (tag == null) {
            return;
        }
        
        switch (tag) {
            case 10:
                // 入仓前费用
                profitReport.setFbsCheckInCost(serviceCost);
                break;
            case 20:
                // 出单后操作费
                profitReport.setFbsOperatingCost(serviceCost);
                break;
            default:
                // 其他费用可以根据需要扩展
                log.debug("未处理的FBS费用标签: tag={}, cost={}", tag, serviceCost);
                break;
        }
    }
    
    /**
     * 计算FBO仓储费用
     * 统一自统计成本为负数
     */
    private void calculateFboCosts(ProfitReportDO profitReport, ProductCostsDO productCosts,
                                 ProductPurchaseDO productPurchase, Integer quantity, CalculationConfig config) {
        if (productCosts == null || quantity == null || quantity <= 0) {
            return;
        }
        // FBO送仓费
        if (productCosts.getFboDeliveryCost() != null) {
            BigDecimal deliveryCost = CalculationUtils.multiply(productCosts.getFboDeliveryCost(), new BigDecimal(quantity), config);
            profitReport.setFboDeliverCost(deliveryCost.negate()); // 统一为负数
        }
        // FBO验收费
        if (productCosts.getFboInspectionCost() != null) {
            BigDecimal inspectionCost = CalculationUtils.multiply(productCosts.getFboInspectionCost(), new BigDecimal(quantity), config);
            profitReport.setFboInspectionCost(inspectionCost.negate()); // 统一为负数
        }
    }
    
    /**
     * 根据单位类型计算费用
     * 现在正确使用CalculationConfig
     */
    private BigDecimal calculateCostByUnitType(Integer unitType, BigDecimal unitPrice, 
                                             ProductPurchaseDO productPurchase, Integer quantity, CalculationConfig config) {
        if (unitType == null || unitPrice == null || quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal baseAmount;
        switch (unitType) {
            case 10: // 按重量
                BigDecimal grossWeight = productPurchase.getGrossWeight() != null ? 
                        CalculationUtils.divide(productPurchase.getGrossWeight(), new BigDecimal("1000"), config) : 
                        BigDecimal.ZERO;
                baseAmount = CalculationUtils.multiply(grossWeight, unitPrice, config);
                break;
            case 20: // 按体积
                BigDecimal volume = calculateVolume(productPurchase, config);
                baseAmount = CalculationUtils.multiply(volume, unitPrice, config);
                break;
            case 50: // 按密度
                BigDecimal weight = productPurchase.getGrossWeight() != null ? 
                        CalculationUtils.divide(productPurchase.getGrossWeight(), new BigDecimal("1000"), config) : 
                        BigDecimal.ZERO;
                BigDecimal vol = calculateVolume(productPurchase, config);
                if (vol.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal density = CalculationUtils.divide(weight, vol, config);
                    baseAmount = CalculationUtils.multiply(density, unitPrice, config);
                } else {
                    baseAmount = BigDecimal.ZERO;
                }
                break;
            default:
                baseAmount = unitPrice;
        }
        
        return CalculationUtils.multiply(baseAmount, new BigDecimal(quantity), config);
    }
    

    
    /**
     * 订单金额分配信息
     */
    @lombok.Data
    @lombok.Builder
    private static class OrderAmountInfo {
        private String postingNumber;
        private BigDecimal totalAmount;
        private String maxAmountSkuId;
        private Map<String, BigDecimal> itemRatioMap;
    }
} 