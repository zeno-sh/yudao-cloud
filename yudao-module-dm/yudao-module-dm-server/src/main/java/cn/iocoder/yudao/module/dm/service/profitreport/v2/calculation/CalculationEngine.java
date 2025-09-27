package cn.iocoder.yudao.module.dm.service.profitreport.v2.calculation;

import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.DataCollectionResult;

/**
 * 计算引擎接口
 * 核心计算逻辑的统一入口
 *
 * @author Jax
 */
public interface CalculationEngine {
    
    /**
     * 执行成本计算
     *
     * @param dataResult 数据收集结果
     * @param taskId 任务ID
     * @return 计算结果
     */
    CalculationResult calculate(DataCollectionResult dataResult, String taskId);
} 