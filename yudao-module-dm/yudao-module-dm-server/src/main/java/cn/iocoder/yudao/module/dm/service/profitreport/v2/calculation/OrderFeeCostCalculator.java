package cn.iocoder.yudao.module.dm.service.profitreport.v2.calculation;

import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationConfig;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.DataCollectionResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.util.CalculationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收单费用计算器
 * 处理type='other'且operation_type='MarketplaceRedistributionOfAcquiringOperation'的账单记录
 * 使用前缀匹配逻辑精确归属到具体SKU，参考V1利润测算逻辑增加兜底处理机制
 * 
 * 根据数据层复用设计方案，直接输出ProfitReportDO格式
 *
 * @author Jax
 */
@Component
@Slf4j
public class OrderFeeCostCalculator implements CostCalculator {
    
    @Resource
    private OzonOrderService ozonOrderService;
    
    @Override
    public CostCalculatorType getType() {
        return CostCalculatorType.ORDER_FEE;
    }
    
    @Override
    public boolean supports(DataCollectionResult dataResult) {
        return dataResult.getCostData().getFinanceTransactions() != null &&
               dataResult.getCostData().getFinanceTransactions().stream()
                       .anyMatch(t -> "other".equals(t.getType()));
    }
    
    @Override
    public List<ProfitReportDO> calculate(
            DataCollectionResult dataResult, 
            CalculationConfig config, 
            String taskId) {
        
        log.info("开始计算收单费用: taskId={}", taskId);
        
        List<ProfitReportDO> results = new ArrayList<>();
        
        try {
            // 获取收单费用账单记录
            List<OzonFinanceTransactionDO> orderFeeTransactions = dataResult.getCostData().getFinanceTransactions()
                    .stream()
                    .filter(t -> "other".equals(t.getType()))
                    .collect(Collectors.toList());
            
            if (orderFeeTransactions.isEmpty()) {
                log.info("没有找到收单费用账单记录: taskId={}", taskId);
                return results;
            }
            
            // 直接根据orderNumber反查数据库处理收单费用
            processOrderFeesByDatabase(orderFeeTransactions, dataResult.getProductData().getPlatformSkuIdMapping(), results, config);
            
            log.info("收单费用计算完成: taskId={}, 处理收单费用账单数={}, 生成成本明细数={}", 
                    taskId, orderFeeTransactions.size(), results.size());
            
        } catch (Exception e) {
            log.error("收单费用计算失败: taskId={}", taskId, e);
            throw new RuntimeException("收单费用计算失败", e);
        }
        
        return results;
    }
    
    /**
     * 直接根据orderNumber反查数据库处理收单费用
     */
    private void processOrderFeesByDatabase(List<OzonFinanceTransactionDO> orderFeeTransactions,
                                          Map<String, Long> platformSkuIdMapping,
                                          List<ProfitReportDO> results,
                                          CalculationConfig config) {
        
        // 1. 提取所有orderNumber（posting_number实际是order_number）
        List<String> orderNumbers = orderFeeTransactions.stream()
                .map(OzonFinanceTransactionDO::getPostingNumber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (orderNumbers.isEmpty()) {
            log.warn("收单费用账单记录中没有有效的orderNumber");
            return;
        }
        
        // 2. 提取clientIds
        List<String> clientIds = orderFeeTransactions.stream()
                .map(OzonFinanceTransactionDO::getClientId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        log.info("根据orderNumber查询订单: orderNumbers={}, clientIds={}", orderNumbers, clientIds);
        
        // 3. 根据orderNumber查询订单表
        List<OzonOrderDO> orders = ozonOrderService.batchOrderListByOrderNumbers(clientIds, orderNumbers);
        
        if (orders.isEmpty()) {
            log.warn("根据orderNumber未找到任何订单: {}", orderNumbers);
            return;
        }
        
        // 4. 根据订单的posting_number查询订单明细表
        List<String> postingNumbers = orders.stream()
                .map(OzonOrderDO::getPostingNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        List<OzonOrderItemDO> orderItems = ozonOrderService.batchOrderItemListByPostingNumbers(
                clientIds.toArray(new String[0]), postingNumbers);
        
        // 5. 构建orderNumber到订单明细的映射
        Map<String, List<OzonOrderItemDO>> orderNumberToItemsMap = new HashMap<>();
        Map<String, String> postingToOrderNumberMap = orders.stream()
                .collect(Collectors.toMap(OzonOrderDO::getPostingNumber, OzonOrderDO::getOrderNumber));
        
        for (OzonOrderItemDO item : orderItems) {
            String orderNumber = postingToOrderNumberMap.get(item.getPostingNumber());
            if (orderNumber != null) {
                orderNumberToItemsMap.computeIfAbsent(orderNumber, k -> new ArrayList<>()).add(item);
            }
        }
        
        log.info("查询到订单数={}, 订单明细数={}, orderNumber映射数={}", 
                orders.size(), orderItems.size(), orderNumberToItemsMap.size());
        
        // 6. 处理每个收单费用交易
        for (OzonFinanceTransactionDO transaction : orderFeeTransactions) {
            String orderNumber = transaction.getPostingNumber(); // 实际是order_number
            
            List<OzonOrderItemDO> relatedItems = orderNumberToItemsMap.get(orderNumber);
            
            if (relatedItems != null && !relatedItems.isEmpty()) {
                // 找到了相关的订单明细，按V1逻辑处理
                if (relatedItems.size() == 1) {
                    // 单商品订单，直接分配
                    OzonOrderItemDO orderItem = relatedItems.get(0);
                    String platformSkuId = orderItem.getPlatformSkuId();
                    Long dmProductId = platformSkuIdMapping.get(platformSkuId);
                    
                    if (dmProductId != null) {
                        ProfitReportDO profitReport = createOrderFeeProfitReportDO(orderItem, dmProductId, transaction);
                        if (profitReport != null) {
                            profitReport.setOrderFeeCost(transaction.getAmount());
                            setProfitReportCurrencies(profitReport);
                            profitReport.setOrders(0);
                            profitReport.setSalesVolume(0);
                            
                            results.add(profitReport);
                            
                            log.debug("收单费用处理成功(单商品): orderNumber={} -> platformSkuId={}, 费用金额={}", 
                                    orderNumber, platformSkuId, transaction.getAmount());
                        }
                    } else {
                        log.warn("平台SKU未找到对应的产品ID: platformSkuId={}", platformSkuId);
                    }
                } else {
                    // 多商品订单，按比例分配
                    BigDecimal totalOrderValue = relatedItems.stream()
                            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    if (totalOrderValue.compareTo(BigDecimal.ZERO) > 0) {
                        for (OzonOrderItemDO orderItem : relatedItems) {
                            String platformSkuId = orderItem.getPlatformSkuId();
                            Long dmProductId = platformSkuIdMapping.get(platformSkuId);
                            
                            if (dmProductId != null) {
                                // 计算该商品的价值占比
                                BigDecimal itemValue = orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()));
                                BigDecimal ratio = itemValue.divide(totalOrderValue, 4, RoundingMode.HALF_UP);
                                BigDecimal allocatedFee = transaction.getAmount().multiply(ratio);
                                
                                ProfitReportDO profitReport = createOrderFeeProfitReportDO(orderItem, dmProductId, transaction);
                                if (profitReport != null) {
                                    profitReport.setOrderFeeCost(allocatedFee);
                                    setProfitReportCurrencies(profitReport);
                                    profitReport.setOrders(0);
                                    profitReport.setSalesVolume(0);
                                    
                                    results.add(profitReport);
                                    
                                    log.debug("收单费用处理成功(多商品): orderNumber={} -> platformSkuId={}, 比例={}, 分配金额={}", 
                                            orderNumber, platformSkuId, ratio, allocatedFee);
                                }
                            } else {
                                log.warn("平台SKU未找到对应的产品ID: platformSkuId={}", platformSkuId);
                            }
                        }
                    }
                }
            } else {
                // 无法找到相关订单明细，记录为无法处理的费用
                log.warn("无法找到orderNumber {} 对应的订单明细信息，收单费用 {} 将被忽略", 
                        orderNumber, transaction.getAmount());
            }
        }
    }
    
    /**
     * 创建收单费用ProfitReportDO基础对象
     */
    private ProfitReportDO createOrderFeeProfitReportDO(OzonOrderItemDO orderItem, Long dmProductId, 
                                                       OzonFinanceTransactionDO transaction) {
        
        // 修复：不使用当前日期，如果没有有效的操作日期则跳过
        if (transaction.getOperationDate() == null) {
            log.warn("收单费用账单记录没有有效的操作日期，跳过该记录: orderNumber={}, platformSkuId={}", 
                    transaction.getPostingNumber(), orderItem.getPlatformSkuId());
            return null;
        }
        
        LocalDate financeDate = transaction.getOperationDate();
        
        ProfitReportDO profitReport = new ProfitReportDO();
        profitReport.setClientId(transaction.getClientId());
        profitReport.setProductId(dmProductId);
        profitReport.setPlatformSkuId(orderItem.getPlatformSkuId());
        profitReport.setOfferId(orderItem.getOfferId());
        profitReport.setFinanceDate(financeDate);
        
        return profitReport;
    }
    
    /**
     * 设置ProfitReportDO的币种信息
     * 使用字典获取币种编码
     */
    private void setProfitReportCurrencies(ProfitReportDO profitReport) {
        // 从字典获取币种编码
        Integer rubCurrency = Integer.valueOf(DictFrameworkUtils.parseDictDataValue("dm_currency_code", "RUB"));
        
        // 平台币种默认为卢布
        profitReport.setPlatformCurrency(rubCurrency);
        profitReport.setFbsCurrency(rubCurrency);
    }
} 