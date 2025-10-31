package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 计算元数据
 * 用于记录计算过程中的关键信息
 *
 * @author Jax
 */
@Data
@Builder
public class CalculationMetadata {
    
    /**
     * 计算任务ID
     */
    private String taskId;
    
    /**
     * 计算时间
     */
    private LocalDateTime calculationTime;
    
    /**
     * 计算版本
     */
    @Builder.Default
    private String calculationVersion = "2.0";
    
    /**
     * 使用的汇率
     */
    private Map<String, String> exchangeRates;
    
    /**
     * 计算精度
     */
    private Integer precision;
    
    /**
     * 数据来源
     */
    private String dataSource;
    
    /**
     * 计算配置标识
     */
    private String configVersion;
    
    /**
     * 数据完整性标识
     */
    private String dataIntegrity;
    
    /**
     * 警告信息
     */
    private String[] warnings;
    
    /**
     * 计算类型
     */
    private String calculationType;
    
    /**
     * 订单号
     */
    private String postingNumber;
    
    /**
     * 订单类型
     */
    private Integer orderType;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 原始订单号
     */
    private String orderNumber;
} 