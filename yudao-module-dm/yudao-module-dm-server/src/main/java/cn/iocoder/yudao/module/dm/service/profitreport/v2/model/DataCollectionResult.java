package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.DateRange;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据收集结果
 *
 * @author Jax
 */
@Data
@Builder
public class DataCollectionResult {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 日期范围
     */
    private DateRange dateRange;
    
    /**
     * 数据收集时间
     */
    private LocalDateTime collectionTime;
    
    /**
     * 订单数据
     */
    private OrderData orderData;
    
    /**
     * 产品数据
     */
    private ProductData productData;
    
    /**
     * 成本数据
     */
    private CostData costData;
    
    /**
     * 汇率数据
     */
    private ExchangeRateData exchangeRateData;
    
    /**
     * 收集的数据集总数
     */
    private Integer totalDataSets;
    
    /**
     * 所有成本明细（用于利润计算器）
     */
    private List<ProfitReportDO> allCostDetails;
    
    /**
     * 获取所有成本明细，如果为空则返回空列表
     */
    public List<ProfitReportDO> getAllCostDetails() {
        return allCostDetails != null ? allCostDetails : new ArrayList<>();
    }
} 