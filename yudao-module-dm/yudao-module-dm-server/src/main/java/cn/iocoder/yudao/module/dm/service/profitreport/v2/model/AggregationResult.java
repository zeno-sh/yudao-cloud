package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聚合结果
 *
 * @author Jax
 */
@Data
@Builder
public class AggregationResult {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 聚合开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 聚合结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 聚合后的利润报告结果列表
     */
    private List<Object> aggregatedResults;
    
    /**
     * 总记录数
     */
    private Integer totalRecords;
    
    /**
     * 按维度分组的记录数统计
     */
    private Map<String, Integer> recordsByDimension;
    
    /**
     * 聚合汇总数据
     */
    private Map<String, Object> summaryData;
    
    /**
     * 聚合配置
     */
    private Object aggregationConfig;
    
    /**
     * 数据完整性检查结果
     */
    private Map<String, Boolean> integrityChecks;
} 