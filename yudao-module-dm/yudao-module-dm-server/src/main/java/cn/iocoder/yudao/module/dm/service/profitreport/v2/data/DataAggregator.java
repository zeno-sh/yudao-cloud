package cn.iocoder.yudao.module.dm.service.profitreport.v2.data;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitCalculationRequestVO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.AggregationResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationResult;

/**
 * 数据聚合器接口
 * 负责对计算结果进行聚合和格式化
 *
 * @author Jax
 */
public interface DataAggregator {
    
    /**
     * 聚合计算结果
     *
     * @param calculationResult 计算结果
     * @param request 原始请求参数
     * @param taskId 任务ID
     * @return 聚合结果
     */
    AggregationResult aggregate(CalculationResult calculationResult, ProfitCalculationRequestVO request, String taskId);
} 