package cn.iocoder.yudao.module.dm.service.profitreport.v2.data;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitCalculationRequestVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeServicesDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import cn.iocoder.yudao.module.dm.service.logistics.FbsFeeServicesService;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CostData;
import cn.iocoder.yudao.module.dm.service.productcosts.ProductCostsService;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.service.transaction.OzonFinanceTransactionService;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成本数据收集器
 * 负责收集产品成本配置、财务交易记录、FBS仓储费用等成本相关数据
 * 
 * 重要修复：支持接收预先收集的财务交易数据，避免重复收集
 *
 * @author Jax
 */
@Component
@Slf4j
public class CostDataCollector {
    
    @Resource
    private ProductCostsService productCostsService;
    @Resource
    private OzonFinanceTransactionService financeTransactionService;
    @Resource
    private FbsWarehouseService fbsWarehouseService;
    @Resource
    private FbsFeeServicesService fbsFeeServicesService;
    @Resource
    private OzonProductOnlineService ozonProductOnlineService;
    
    /**
     * 收集成本数据（修复版本：使用预先收集的财务交易数据）
     *
     * @param request 计算请求参数
     * @param taskId 任务ID
     * @param financeTransactions 预先收集的财务交易数据
     * @return 成本数据
     */
    public CostData collectWithFinanceTransactions(
            ProfitCalculationRequestVO request, 
            String taskId,
            List<OzonFinanceTransactionDO> financeTransactions) {
        
        // 只取第一个clientId
        String clientId = (request.getClientIds() != null && request.getClientIds().length > 0) 
                ? request.getClientIds()[0] : null;
        
        log.info("开始收集成本数据（使用预先收集的财务交易数据）: clientId={}, 交易记录数={}, taskId={}", 
                clientId, financeTransactions.size(), taskId);
        
        if (clientId == null || request.getFinanceDate() == null || request.getFinanceDate().length < 2) {
            throw new IllegalArgumentException("clientId和日期范围不能为空");
        }
        
        try {
            // 收集产品成本配置数据：先获取门店的产品列表，再批量获取成本配置
            List<OzonProductOnlineDO> onlineProducts = ozonProductOnlineService.getAllProductOnlineByClientId(clientId);
            List<Long> productIds = onlineProducts.stream()
                    .map(OzonProductOnlineDO::getDmProductId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<ProductCostsDO> productCosts = Collections.emptyList();
            if (!productIds.isEmpty()) {
                productCosts = productCostsService.batchProductCostsListByProductIds(productIds);
            }
            log.info("收集到产品成本配置: {} 条, taskId={}", productCosts.size(), taskId);
            
            // 使用预先收集的财务交易记录（避免重复收集）
            log.info("使用预先收集的财务交易记录: {} 条, taskId={}", financeTransactions.size(), taskId);
            
            // 收集FBS仓库配置（获取所有仓库）
            List<FbsWarehouseDO> fbsWarehouses = fbsWarehouseService.getAllFbsWarehouse();
            log.info("收集到FBS仓库配置: {} 条, taskId={}", fbsWarehouses.size(), taskId);
            
            // 收集FBS费用服务配置（基于仓库ID获取）
            List<Long> warehouseIds = fbsWarehouses.stream()
                    .map(FbsWarehouseDO::getId)
                    .collect(Collectors.toList());
            Map<Long, List<FbsFeeServicesDO>> feeServicesMap = 
                    fbsFeeServicesService.batchFbsFeeServices(warehouseIds);
            List<FbsFeeServicesDO> fbsFeeServices = feeServicesMap.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            log.info("收集到FBS费用服务配置: {} 条, taskId={}", fbsFeeServices.size(), taskId);
            
            // 构建产品ID到成本配置的映射
            Map<Long, ProductCostsDO> productCostsMap = productCosts.stream()
                    .collect(Collectors.toMap(
                            ProductCostsDO::getProductId,
                            cost -> cost, 
                            (cost1, cost2) -> cost1));
            
            // 构建仓库ID到仓库配置的映射
            Map<Long, FbsWarehouseDO> warehouseMap = fbsWarehouses.stream()
                    .collect(Collectors.toMap(
                            FbsWarehouseDO::getId,
                            warehouse -> warehouse,
                            (w1, w2) -> w1));
            
            return CostData.builder()
                    .productCosts(productCosts)
                    .financeTransactions(financeTransactions) // 使用预先收集的数据
                    .fbsWarehouses(fbsWarehouses)
                    .fbsFeeServices(fbsFeeServices)
                    .productCostsMap(productCostsMap)
                    .warehouseMap(warehouseMap)
                    .totalTransactionCount(financeTransactions.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("收集成本数据失败: clientId={}, taskId={}", clientId, taskId, e);
            throw new RuntimeException("成本数据收集失败", e);
        }
    }
} 