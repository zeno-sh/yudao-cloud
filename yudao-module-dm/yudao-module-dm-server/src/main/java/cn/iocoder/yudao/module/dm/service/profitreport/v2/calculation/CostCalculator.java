package cn.iocoder.yudao.module.dm.service.profitreport.v2.calculation;

import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.DataCollectionResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationConfig;

import java.util.List;

/**
 * 成本计算器接口
 * 根据数据层复用设计方案，直接返回ProfitReportDO
 *
 * @author Jax
 */
public interface CostCalculator {
    
    /**
     * 计算器类型
     */
    CostCalculatorType getType();
    
    /**
     * 是否支持该请求
     */
    boolean supports(DataCollectionResult dataResult);
    
    /**
     * 执行成本计算
     */
    List<ProfitReportDO> calculate(
        DataCollectionResult dataResult, 
        CalculationConfig config, 
        String taskId
    );
} 