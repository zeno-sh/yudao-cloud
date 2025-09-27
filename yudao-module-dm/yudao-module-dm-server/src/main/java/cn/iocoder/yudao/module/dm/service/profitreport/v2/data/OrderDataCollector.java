package cn.iocoder.yudao.module.dm.service.profitreport.v2.data;

import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitCalculationRequestVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.OrderData;
import cn.iocoder.yudao.module.dm.service.transaction.OzonFinanceTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 订单数据收集器
 * 负责收集指定时间范围内的订单和订单明细数据
 * 
 * 重要修复：支持接收预先收集的财务交易数据，避免重复收集导致记录翻倍
 *
 * @author Jax
 */
@Component
@Slf4j
public class OrderDataCollector {
    
    @Resource
    private OzonOrderService ozonOrderService;
    @Resource
    private OzonFinanceTransactionService financeTransactionService;
    
    /**
     * 收集订单数据（修复版本：使用预先收集的财务交易数据）
     * 
     * @param request 计算请求参数
     * @param taskId 任务ID
     * @param financeTransactions 预先收集的财务交易数据
     * @return 订单数据
     */
    public OrderData collectWithFinanceTransactions(
            ProfitCalculationRequestVO request, 
            String taskId, 
            List<OzonFinanceTransactionDO> financeTransactions) {
        
        // 只取第一个clientId
        String clientId = (request.getClientIds() != null && request.getClientIds().length > 0) 
                ? request.getClientIds()[0] : null;
        
        log.info("开始收集订单数据（使用预先收集的财务交易数据）: clientId={}, 交易记录数={}, taskId={}", 
                clientId, financeTransactions.size(), taskId);
        
        if (clientId == null || request.getFinanceDate() == null || request.getFinanceDate().length < 2) {
            throw new IllegalArgumentException("clientId和日期范围不能为空");
        }
        
        LocalDate startDate = LocalDate.parse(request.getFinanceDate()[0]);
        LocalDate endDate = LocalDate.parse(request.getFinanceDate()[1]);
        
        try {
            // 从财务交易记录中筛选"订单"类型的记录，提取posting_number
            List<OzonFinanceTransactionDO> orderTransactions = financeTransactions.stream()
                    .filter(transaction -> transaction.getType().equals(DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "订单")))
                    .collect(Collectors.toList());
            
            List<String> postingNumbers = orderTransactions.stream()
                    .map(OzonFinanceTransactionDO::getPostingNumber)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            log.info("从财务交易记录提取到posting_number: {} 个, taskId={}", postingNumbers.size(), taskId);
            
            // 根据posting_number查询对应的订单
            List<OzonOrderDO> orders = new ArrayList<>();
            if (!postingNumbers.isEmpty()) {
                orders = ozonOrderService.batchOrderListByPostingNumbers(postingNumbers);
            }
            log.info("根据posting_number查询到订单数据: {} 条, taskId={}", orders.size(), taskId);
            
            // 根据订单的posting_number查询订单明细
            List<OzonOrderItemDO> orderItems = new ArrayList<>();
            if (!orders.isEmpty()) {
                List<String> orderPostingNumbers = orders.stream()
                        .map(OzonOrderDO::getPostingNumber)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                
                orderItems = ozonOrderService.batchOrderItemListByPostingNumbers(
                        new String[]{clientId}, orderPostingNumbers);
            }
            log.info("根据订单posting_number查询到订单明细数据: {} 条, taskId={}", orderItems.size(), taskId);
            
            return OrderData.builder()
                    .orders(orders)
                    .orderItems(orderItems)
                    .clientId(clientId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalOrderCount(orders.size())
                    .totalItemCount(orderItems.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("收集订单数据失败: clientId={}, taskId={}", clientId, taskId, e);
            throw new RuntimeException("订单数据收集失败", e);
        }
    }
    
    /**
     * 收集订单数据（旧版本：重新收集财务交易数据）
     * 
     * @deprecated 此方法会重复收集财务交易数据，请使用 collectWithFinanceTransactions 方法
     * 
     * @param request 计算请求参数
     * @param taskId 任务ID
     * @return 订单数据
     */
    @Deprecated
    public OrderData collect(ProfitCalculationRequestVO request, String taskId) {
        // 只取第一个clientId
        String clientId = (request.getClientIds() != null && request.getClientIds().length > 0) 
                ? request.getClientIds()[0] : null;
        
        log.warn("使用了已废弃的collect方法，可能导致重复收集数据: clientId={}, taskId={}", clientId, taskId);
        
        if (clientId == null || request.getFinanceDate() == null || request.getFinanceDate().length < 2) {
            throw new IllegalArgumentException("clientId和日期范围不能为空");
        }
        
        LocalDate startDate = LocalDate.parse(request.getFinanceDate()[0]);
        LocalDate endDate = LocalDate.parse(request.getFinanceDate()[1]);
        
        try {
            // 重新收集财务交易记录（这是导致重复的根源）
            List<OzonFinanceTransactionDO> financeTransactions = financeTransactionService.getSigendTransactionList(
                    Collections.singletonList(clientId), 
                    new LocalDate[]{startDate, endDate});
            log.info("重新收集到财务交易记录: {} 条, taskId={}", financeTransactions.size(), taskId);
            
            // 委托给新的方法处理
            return collectWithFinanceTransactions(request, taskId, financeTransactions);
                    
        } catch (Exception e) {
            log.error("收集订单数据失败: clientId={}, taskId={}", clientId, taskId, e);
            throw new RuntimeException("订单数据收集失败", e);
        }
    }
} 