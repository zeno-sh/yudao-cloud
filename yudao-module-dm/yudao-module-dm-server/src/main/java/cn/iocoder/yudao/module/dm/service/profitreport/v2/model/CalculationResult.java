package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 计算结果
 *
 * @author Jax
 */
@Data
@Builder
public class CalculationResult {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 计算开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 计算结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 成本明细列表
     */
    private List<ProfitReportDO> costDetails;
    
    /**
     * 利润明细列表
     */
    private List<ProfitReportDO> profitDetails;
    
    /**
     * 计算统计信息
     */
    private Map<String, Integer> calculationStats;
    
    /**
     * 计算元数据
     */
    private CalculationMetadata calculationMetadata;
    
    /**
     * 计算过程中的异常记录
     */
    private List<String> exceptions;
    
    /**
     * 计算精度
     */
    private Integer precision;
} 