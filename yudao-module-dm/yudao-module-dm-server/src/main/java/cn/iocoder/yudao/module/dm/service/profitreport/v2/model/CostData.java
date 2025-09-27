package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeServicesDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 成本数据
 *
 * @author Jax
 */
@Data
@Builder
public class CostData {
    
    /**
     * 产品成本配置列表
     */
    private List<ProductCostsDO> productCosts;
    
    /**
     * 财务交易记录列表
     */
    private List<OzonFinanceTransactionDO> financeTransactions;
    
    /**
     * FBS仓库配置列表
     */
    private List<FbsWarehouseDO> fbsWarehouses;
    
    /**
     * FBS费用服务配置列表
     */
    private List<FbsFeeServicesDO> fbsFeeServices;
    
    /**
     * 产品ID到成本配置的映射
     */
    private Map<Long, ProductCostsDO> productCostsMap;
    
    /**
     * 仓库ID到仓库配置的映射
     */
    private Map<Long, FbsWarehouseDO> warehouseMap;
    
    /**
     * 财务交易记录总数
     */
    private Integer totalTransactionCount;
}