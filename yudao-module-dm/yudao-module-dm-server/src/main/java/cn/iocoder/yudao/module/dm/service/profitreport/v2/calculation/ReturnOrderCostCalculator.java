package cn.iocoder.yudao.module.dm.service.profitreport.v2.calculation;

import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ServiceDTO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationConfig;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.DataCollectionResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.util.CalculationUtils;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.util.ProfitCalculationLogger;
import com.alibaba.fastjson2.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSONObject;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 退货成本计算器
 * 处理type='returns'的账单记录
 *
 * @author Jax
 */
@Component
@Slf4j
public class ReturnOrderCostCalculator implements CostCalculator {

    @Resource
    private ProfitCalculationLogger profitCalculationLogger;

    @Override
    public CostCalculatorType getType() {
        return CostCalculatorType.RETURN_ORDER;
    }

    @Override
    public boolean supports(DataCollectionResult dataResult) {
        return dataResult.getCostData().getFinanceTransactions() != null &&
                dataResult.getCostData().getFinanceTransactions().stream()
                        .anyMatch(t -> "returns".equals(t.getType()));
    }

    @Override
    public List<ProfitReportDO> calculate(
            DataCollectionResult dataResult,
            CalculationConfig config,
            String taskId) {

        log.info("开始计算退货成本: taskId={}", taskId);

        List<ProfitReportDO> results = new ArrayList<>();

        // 获取基础数据
        List<OzonFinanceTransactionDO> returnTransactions = dataResult.getCostData().getFinanceTransactions()
                .stream()
                .filter(t -> "returns".equals(t.getType()))
                .collect(Collectors.toList());

        Map<Long, ProductCostsDO> productCostsMap = dataResult.getCostData().getProductCostsMap();
        Map<Long, ProductPurchaseDO> productPurchaseMap = dataResult.getProductData().getProductPurchases();
        Map<String, Long> platformSkuIdMapping = dataResult.getProductData().getPlatformSkuIdMapping();
        List<OzonOrderItemDO> allOrderItems = dataResult.getOrderData().getAllOrderItems();

        // 1. 从账单找到订单号，订单号找到SKU，按SKU分组账单List
        Map<Long, List<OzonFinanceTransactionDO>> transactionsBySku = groupTransactionsBySku(
                returnTransactions, allOrderItems, platformSkuIdMapping, taskId);

        // 2. 为每个SKU累计指标相关的金额
        for (Map.Entry<Long, List<OzonFinanceTransactionDO>> entry : transactionsBySku.entrySet()) {
            Long sku = entry.getKey();
            List<OzonFinanceTransactionDO> skuTransactions = entry.getValue();

            // 获取SKU对应的产品ID
            String platformSkuId = String.valueOf(sku);
            Long dmProductId = platformSkuIdMapping.get(platformSkuId);

            if (dmProductId == null) {
                profitCalculationLogger.logProductNotMapped(taskId, platformSkuId);
                continue;
            }

            // 处理该SKU下的所有退货账单
            ProfitReportDO profitReport = processReturnTransactionsForSku(taskId, sku, skuTransactions,
                    allOrderItems, productCostsMap.get(dmProductId), productPurchaseMap.get(dmProductId), config);

            if (profitReport != null) {
                results.add(profitReport);
            }
        }

        log.info("退货成本计算完成: taskId={}, 处理退货账单数={}, 涉及SKU数={}, 生成成本明细数={}",
                taskId, returnTransactions.size(), transactionsBySku.size(), results.size());

        return results;
    }

    /**
     * 按SKU分组退货账单
     * 1. 从账单找到订单号
     * 2. 订单号找到SKU
     * 3. 按SKU分组账单List，拿到SKU和List账单映射
     */
    private Map<Long, List<OzonFinanceTransactionDO>> groupTransactionsBySku(
            List<OzonFinanceTransactionDO> returnTransactions,
            List<OzonOrderItemDO> allOrderItems,
            Map<String, Long> platformSkuIdMapping,
            String taskId) {

        Map<Long, List<OzonFinanceTransactionDO>> transactionsBySku = new HashMap<>();

        for (OzonFinanceTransactionDO transaction : returnTransactions) {
            Set<Long> skusForTransaction = new HashSet<>();

            // 方法1：从交易记录的items字段中直接提取SKU
            Set<Long> directSkus = extractSkusFromTransaction(transaction);
            skusForTransaction.addAll(directSkus);

            // 方法2：通过posting_number从订单明细中查找SKU
            if (transaction.getPostingNumber() != null) {
                List<OzonOrderItemDO> relatedOrderItems = findRelatedOrderItems(
                        transaction.getPostingNumber(), allOrderItems);
                for (OzonOrderItemDO orderItem : relatedOrderItems) {
                    try {
                        Long sku = Long.valueOf(orderItem.getPlatformSkuId());
                        // 验证SKU是否在映射中存在
                        if (platformSkuIdMapping.containsKey(String.valueOf(sku))) {
                            skusForTransaction.add(sku);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("无效的platformSkuId: {}", orderItem.getPlatformSkuId());
                    }
                }
            }

            // 如果找不到任何SKU，记录警告
            if (skusForTransaction.isEmpty()) {
                log.warn("账单无法关联到任何SKU: postingNumber={}, operationType={}, amount={}",
                        transaction.getPostingNumber(), transaction.getOperationType(), transaction.getAmount());
                continue;
            }

            // 将账单分配到对应的SKU组中
            for (Long sku : skusForTransaction) {
                transactionsBySku.computeIfAbsent(sku, k -> new ArrayList<>()).add(transaction);
            }
        }

        log.info("按SKU分组完成: taskId={}, 总账单数={}, 涉及SKU数={}",
                taskId, returnTransactions.size(), transactionsBySku.size());

        return transactionsBySku;
    }

    /**
     * 处理单个SKU下的所有退货账单
     * 累计SKU对应的指标相关的金额
     */
    private ProfitReportDO processReturnTransactionsForSku(
            String taskId,
            Long sku,
            List<OzonFinanceTransactionDO> skuTransactions,
            List<OzonOrderItemDO> allOrderItems,
            ProductCostsDO productCosts,
            ProductPurchaseDO productPurchase,
            CalculationConfig config) {

        String platformSkuId = String.valueOf(sku);

        // 查找对应的订单明细
        OzonOrderItemDO orderItem = findOrderItemBySku(sku, skuTransactions, allOrderItems);
        if (orderItem == null) {
            // 如果找不到订单明细，创建基础订单项
            String postingNumber = skuTransactions.isEmpty() ? null : skuTransactions.get(0).getPostingNumber();
            orderItem = createBasicOrderItem(platformSkuId, postingNumber);
        }

        // 创建ProfitReportDO
        ProfitReportDO profitReport = createReturnProfitReportDO(orderItem, 
                Long.valueOf(platformSkuId), skuTransactions);
        setProfitReportCurrencies(profitReport, productCosts);

        // 累计该SKU下所有账单的指标金额
        for (OzonFinanceTransactionDO transaction : skuTransactions) {
            accumulateTransactionAmounts(profitReport, transaction, config);
        }

        // 检查是否有实际退货，如果有则计算基础成本减少
        boolean hasActualRefund = profitReport.getCancelledAmount() != null &&
                profitReport.getCancelledAmount().compareTo(BigDecimal.ZERO) > 0;

        if (hasActualRefund) {
            profitReport.setRefundOrders(1);

            // 计算基础成本减少
            if (productCosts != null && orderItem != null) {
                calculateReturnedProductBasicCostReduction(profitReport, productCosts, productPurchase,
                        Math.abs(orderItem.getQuantity()), config);
            }
        }

        log.debug("处理SKU退货完成: sku={}, 账单数={}, 取消金额={}, 佣金退还={}",
                sku, skuTransactions.size(), profitReport.getCancelledAmount(), profitReport.getReturnCommissionAmount());

        return profitReport;
    }

    /**
     * 从交易记录中提取SKU信息
     */
    private Set<Long> extractSkusFromTransaction(OzonFinanceTransactionDO transaction) {
        Set<Long> skus = new HashSet<>();

        if (transaction.getItems() != null) {
            try {
                JSONArray itemsArray = JSONArray.parseArray(transaction.getItems());
                for (int i = 0; i < itemsArray.size(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    Long sku = item.getLong("sku");
                    if (sku != null) {
                        skus.add(sku);
                    }
                }
            } catch (Exception e) {
                log.warn("解析交易记录items字段失败: {}", transaction.getItems(), e);
            }
        }

        return skus;
    }



    /**
     * 累计单个账单的金额到ProfitReportDO中
     * 按照指标维度累加金额
     */
    private void accumulateTransactionAmounts(ProfitReportDO profitReport,
                                              OzonFinanceTransactionDO transaction,
                                              CalculationConfig config) {

        String operationType = transaction.getOperationType();
        
        // 处理销售金额退还（累加）
        if (transaction.getAccrualsForSale() != null && transaction.getAccrualsForSale().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal cancelAmount = transaction.getAccrualsForSale().abs(); // 转为正数
            BigDecimal currentCancelled = profitReport.getCancelledAmount() != null ?
                    profitReport.getCancelledAmount() : BigDecimal.ZERO;
            profitReport.setCancelledAmount(currentCancelled.add(cancelAmount));
        }

        // 处理佣金退还（累加）
        if (transaction.getSaleCommission() != null && transaction.getSaleCommission().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal returnCommission = transaction.getSaleCommission();
            BigDecimal currentCommission = profitReport.getReturnCommissionAmount() != null ?
                    profitReport.getReturnCommissionAmount() : BigDecimal.ZERO;
            profitReport.setReturnCommissionAmount(currentCommission.add(returnCommission));
        }

        // 处理services字段中的物流费用（累加）
        if (transaction.getServices() != null) {
            parseAndSetReturnLogisticsCosts(profitReport, transaction.getServices(), config);
        }
        
        if ("ClientReturnAgentOperation".equals(operationType)) {
            log.debug("处理客户退货账单: 取消金额={}, 佣金退还={}",
                    transaction.getAccrualsForSale(), transaction.getSaleCommission());
        }
    }

    /**
     * 根据SKU查找对应的订单明细
     */
    private OzonOrderItemDO findOrderItemBySku(Long sku, List<OzonFinanceTransactionDO> skuTransactions,
                                                List<OzonOrderItemDO> allOrderItems) {
        String platformSkuId = String.valueOf(sku);
        
        // 首先尝试通过posting_number精确匹配
        for (OzonFinanceTransactionDO transaction : skuTransactions) {
            if (transaction.getPostingNumber() != null) {
                for (OzonOrderItemDO orderItem : allOrderItems) {
                    if (platformSkuId.equals(orderItem.getPlatformSkuId()) &&
                        transaction.getPostingNumber().equals(orderItem.getPostingNumber())) {
                        return orderItem;
                    }
                }
            }
        }
        
        // 如果精确匹配失败，尝试只匹配SKU
        for (OzonOrderItemDO orderItem : allOrderItems) {
            if (platformSkuId.equals(orderItem.getPlatformSkuId())) {
                return orderItem;
            }
        }
        
        return null;
    }

    /**
     * 创建基础的订单项（当订单明细中找不到时）
     */
    private OzonOrderItemDO createBasicOrderItem(String platformSkuId, String postingNumber) {
        OzonOrderItemDO orderItem = new OzonOrderItemDO();
        orderItem.setPlatformSkuId(platformSkuId);
        orderItem.setPostingNumber(postingNumber);
        orderItem.setQuantity(1); // 默认数量为1
        // 其他字段保持默认值
        return orderItem;
    }

    /**
     * 创建退货ProfitReportDO基础对象
     * 按SKU维度创建，用于累计该SKU下所有账单的金额
     */
    private ProfitReportDO createReturnProfitReportDO(OzonOrderItemDO orderItem, Long dmProductId,
                                                      List<OzonFinanceTransactionDO> transactions) {
        // 寻找有效的操作日期
        LocalDate financeDate = null;
        for (OzonFinanceTransactionDO transaction : transactions) {
            if (transaction.getOperationDate() != null) {
                financeDate = transaction.getOperationDate();
                break;
            }
        }

        ProfitReportDO profitReport = new ProfitReportDO();
        profitReport.setClientId(transactions.get(0).getClientId());
        profitReport.setProductId(dmProductId);
        profitReport.setPlatformSkuId(orderItem.getPlatformSkuId());
        profitReport.setOfferId(orderItem.getOfferId());
        profitReport.setFinanceDate(financeDate);
        profitReport.setOrders(0); // 退货不计入订单数
        profitReport.setRefundOrders(0); // 将在后续处理中设置

        // 设置必要的默认值，避免数据库约束错误
        profitReport.setDeleted(false);
        profitReport.setTenantId(cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId());

        return profitReport;
    }

    /**
     * 查找相关的订单明细
     */
    private List<OzonOrderItemDO> findRelatedOrderItems(String postingNumber, List<OzonOrderItemDO> allOrderItems) {
        // 1. 精确匹配
        List<OzonOrderItemDO> exactMatch = allOrderItems.stream()
                .filter(item -> postingNumber.equals(item.getPostingNumber()))
                .collect(Collectors.toList());

        if (!exactMatch.isEmpty()) {
            return exactMatch;
        }

        // 2. 前缀匹配（退货的posting_number可能只是order_number部分）
        return allOrderItems.stream()
                .filter(item -> item.getPostingNumber() != null &&
                        item.getPostingNumber().startsWith(postingNumber))
                .collect(Collectors.toList());
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
     * 解析services字段并明确分类各种费用
     * MarketplaceServiceItemDelivToCustomer -> 最后一公里费用 (logisticsLastMileCost)
     * MarketplaceServiceItemReturnFlowLogistic -> 逆向物流费用 (reverseLogisticsCost)
     * MarketplaceServiceItemDirectFlowLogistic -> 转运费 (logisticsTransferCost)
     * 其他费用 -> 其他代理服务费 (otherAgentServiceCost)
     */
    private void parseAndSetReturnLogisticsCosts(ProfitReportDO profitReport, String servicesJson, CalculationConfig config) {
        try {
            List<ServiceDTO> serviceDTOS = JSONArray.parseArray(servicesJson, ServiceDTO.class);

            BigDecimal lastMileCost = BigDecimal.ZERO;      // 最后一公里
            BigDecimal reverseLogisticsCost = BigDecimal.ZERO;  // 逆向物流
            BigDecimal transferCost = BigDecimal.ZERO;      // 转运费
            BigDecimal dropOff = BigDecimal.ZERO;
            BigDecimal otherAgentCost = BigDecimal.ZERO;    // 其他代理服务费

            if (CollectionUtils.isNotEmpty(serviceDTOS)) {
                for (ServiceDTO serviceDTO : serviceDTOS) {
                    String serviceName = serviceDTO.getName();
                    BigDecimal price = serviceDTO.getPrice() != null ? serviceDTO.getPrice() : BigDecimal.ZERO;

                    // 明确分类各种费用类型
                    switch (serviceName) {
                        case "MarketplaceServiceItemDelivToCustomer":
                            // 最后一公里费用
                            lastMileCost = lastMileCost.add(price);
                            break;

                        case "MarketplaceServiceItemReturnFlowLogistic":
                            // 逆向物流费用
                            reverseLogisticsCost = reverseLogisticsCost.add(price);
                            break;

                        case "MarketplaceServiceItemDirectFlowLogistic":
                            // 转运费
                            transferCost = transferCost.add(price);
                            break;

                        case "MarketplaceServiceItemDropoffSC":
                            dropOff = dropOff.add(price);

                        default:
                            // 其他代理服务费
                            otherAgentCost = otherAgentCost.add(price);
                            break;
                    }
                }
            }

            // 将各类费用分别累加到对应字段
            if (lastMileCost.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal currentCost = profitReport.getLogisticsLastMileCost() != null ?
                        profitReport.getLogisticsLastMileCost() : BigDecimal.ZERO;
                profitReport.setLogisticsLastMileCost(currentCost.add(lastMileCost));
            }

            if (reverseLogisticsCost.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal currentCost = profitReport.getReverseLogisticsCost() != null ?
                        profitReport.getReverseLogisticsCost() : BigDecimal.ZERO;
                profitReport.setReverseLogisticsCost(currentCost.add(reverseLogisticsCost));
            }

            if (transferCost.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal currentCost = profitReport.getLogisticsTransferCost() != null ?
                        profitReport.getLogisticsTransferCost() : BigDecimal.ZERO;
                profitReport.setLogisticsTransferCost(currentCost.add(transferCost));
            }

            if (otherAgentCost.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal currentCost = profitReport.getOtherAgentServiceCost() != null ?
                        profitReport.getOtherAgentServiceCost() : BigDecimal.ZERO;
                profitReport.setOtherAgentServiceCost(currentCost.add(otherAgentCost));
            }

            if (dropOff.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal currentCost = profitReport.getLogisticsDropOff() != null ?
                        profitReport.getLogisticsDropOff() : BigDecimal.ZERO;
                profitReport.setLogisticsDropOff(currentCost.add(dropOff));
            }

        } catch (Exception e) {
            log.error("解析退货services字段失败: {}", servicesJson, e);
        }
    }

    /**
     * 计算退货商品的基础成本减少
     * 退货时成本冲正应该为正数，用来抵消签收时的负数成本
     * 注意：FBS/FBO仓储费用不能冲正，因为海外仓已经作业，必须收费
     */
    private void calculateReturnedProductBasicCostReduction(ProfitReportDO profitReport, ProductCostsDO productCosts,
                                                            ProductPurchaseDO productPurchase, Integer returnQuantity,
                                                            CalculationConfig config) {
        if (productCosts == null || returnQuantity == null || returnQuantity <= 0) {
            return;
        }
        BigDecimal quantityBD = new BigDecimal(Math.abs(returnQuantity));

        // 采购成本冲正（正数，用来抵消签收时的负数成本）
        if (productCosts.getPurchaseCost() != null) {
            BigDecimal purchaseCostReduction = CalculationUtils.multiply(productCosts.getPurchaseCost(), quantityBD, config);
            profitReport.setPurchaseCost(purchaseCostReduction); // 设置为正数，冲正签收时的负数成本
        }

        // 采购运费冲正（正数）
        if (productCosts.getPurchaseShippingCost() != null && productPurchase != null) {
            BigDecimal shippingCostReduction = calculateCostByUnitType(
                    productCosts.getPurchaseShippingUnit(),
                    productCosts.getPurchaseShippingCost(),
                    productPurchase,
                    Math.abs(returnQuantity),
                    config
            );
            profitReport.setPurchaseShippingCost(shippingCostReduction); // 设置为正数，冲正签收时的负数成本
        }

        // 头程费用冲正（正数）
        if (productCosts.getLogisticsShippingCost() != null && productPurchase != null) {
            BigDecimal logisticsCostReduction = calculateCostByUnitType(
                    productCosts.getLogisticsUnit(),
                    productCosts.getLogisticsShippingCost(),
                    productPurchase,
                    Math.abs(returnQuantity),
                    config
            );
            profitReport.setLogisticsShippingCost(logisticsCostReduction); // 设置为正数，冲正签收时的负数成本
        }

        // 海关成本冲正（关税、VAT等）
        calculateReturnedCustomsCostReduction(profitReport, productCosts, Math.abs(returnQuantity), config);

        // 销售VAT冲正 - 基于取消的销售金额
        calculateReturnedSalesVat(profitReport, productCosts, config);

        log.debug("退货基础成本冲正完成: platformSkuId={}, returnQuantity={}", profitReport.getPlatformSkuId(), returnQuantity);
    }

    /**
     * 计算退货海关成本减少
     * 退货时关税和VAT冲正应该为正数，用来抵消签收时的负数成本
     */
    private void calculateReturnedCustomsCostReduction(ProfitReportDO profitReport, ProductCostsDO productCosts,
                                                       Integer returnQuantity, CalculationConfig config) {

        BigDecimal quantityBD = new BigDecimal(returnQuantity);

        // 申报货值冲正（正数）
        if (productCosts.getCustomsDeclaredValue() != null) {
            BigDecimal declaredValueReduction = CalculationUtils.multiply(productCosts.getCustomsDeclaredValue(), quantityBD, config);
            profitReport.setDeclaredValueCost(declaredValueReduction); // 申报货值在签收时不是负数，所以冲正为正数
        }

        // 关税冲正（正数，冲正签收时的负数关税成本）
        BigDecimal customsCostReduction = BigDecimal.ZERO;
        if (productCosts.getCustomsDuty() != null && productCosts.getCustomsDeclaredValue() != null) {
            BigDecimal dutyRate = CalculationUtils.percentageToDecimal(productCosts.getCustomsDuty(), config);
            BigDecimal declaredValueTotal = CalculationUtils.multiply(productCosts.getCustomsDeclaredValue(), quantityBD, config);
            customsCostReduction = CalculationUtils.multiply(declaredValueTotal, dutyRate, config);
            profitReport.setCustomsCost(customsCostReduction); // 设置为正数，冲正签收时的负数成本
        }

        // 进口VAT冲正（正数，冲正签收时的负数进口VAT成本）
        if (productCosts.getImportVat() != null && productCosts.getCustomsDeclaredValue() != null) {
            BigDecimal declaredValueTotal = CalculationUtils.multiply(productCosts.getCustomsDeclaredValue(), quantityBD, config);
            BigDecimal vatBase = declaredValueTotal.add(customsCostReduction); // 使用关税的正数计算基数
            BigDecimal vatRate = CalculationUtils.percentageToDecimal(productCosts.getImportVat(), config);
            BigDecimal vatCostReduction = CalculationUtils.multiply(vatBase, vatRate, config);
            profitReport.setVatCost(vatCostReduction); // 设置为正数，冲正签收时的负数成本
        }
    }

    /**
     * 计算退货销售VAT冲正
     * 基于取消的销售金额和销售税率计算销售VAT冲正
     * 冲正金额为正数，用来抵消签收时的负数销售VAT成本
     */
    private void calculateReturnedSalesVat(ProfitReportDO profitReport, ProductCostsDO productCosts, CalculationConfig config) {

        if (productCosts == null || productCosts.getSalesTaxRate() == null) {
            return;
        }

        // 检查是否有取消的销售金额
        BigDecimal cancelledAmount = profitReport.getCancelledAmount();
        if (cancelledAmount == null || cancelledAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 计算销售VAT冲正：取消金额 * 销售税率
        BigDecimal salesTaxRate = productCosts.getSalesTaxRate();
        BigDecimal salesTaxRateDecimal = CalculationUtils.percentageToDecimal(salesTaxRate, config);
        BigDecimal salesVatReduction = CalculationUtils.multiply(cancelledAmount, salesTaxRateDecimal, config);

        // 设置为正数，冲正签收时的负数销售VAT成本
        profitReport.setSalesVatCost(salesVatReduction);

        log.debug("销售VAT冲正完成: platformSkuId={}, 取消金额={}, 销售税率={}%, 销售VAT冲正={}",
                profitReport.getPlatformSkuId(), cancelledAmount, salesTaxRate, salesVatReduction);
    }

    /**
     * 根据单位类型计算费用
     * 现在正确使用CalculationConfig
     */
    private BigDecimal calculateCostByUnitType(Integer unitType, BigDecimal unitPrice,
                                               ProductPurchaseDO productPurchase, Integer quantity, CalculationConfig config) {

        if (unitType == null || unitPrice == null || productPurchase == null || quantity == null) {
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


}