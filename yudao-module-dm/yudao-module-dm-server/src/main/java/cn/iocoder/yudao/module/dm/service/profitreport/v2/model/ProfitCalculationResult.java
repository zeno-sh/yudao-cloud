package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 利润计算最终结果
 *
 * @author Jax
 */
@Data
@Builder
public class ProfitCalculationResult {
    
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
     * 计算总耗时（秒）
     */
    private Long duration;
    
    /**
     * 处理的总记录数
     */
    private Integer totalRecords;
    
    /**
     * 成功处理的记录数
     */
    private Integer successRecords;
    
    /**
     * 失败处理的记录数
     */
    private Integer failedRecords;
    
    /**
     * 各阶段处理统计
     */
    private Map<String, Integer> phaseStats;
    
    /**
     * 计算结果存储路径/标识
     */
    private String resultStoragePath;
    
    /**
     * 计算配置版本
     */
    private String configVersion;
    
    /**
     * 数据质量报告
     */
    private Map<String, Object> qualityReport;
    
    /**
     * 警告和错误信息
     */
    private String[] warnings;
    private String[] errors;
    
    /**
     * 是否计算成功
     */
    private Boolean success;
} 