package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import lombok.Builder;
import lombok.Data;

import java.math.RoundingMode;

/**
 * 计算配置
 * 用于配置计算引擎的各种参数
 *
 * @author Jax
 */
@Data
@Builder
public class CalculationConfig {
    
    /**
     * 计算精度（小数位数）
     */
    @Builder.Default
    private Integer precision = 4;
    
    /**
     * 舍入模式
     */
    @Builder.Default
    private RoundingMode roundingMode = RoundingMode.HALF_UP;
    
    /**
     * 是否启用并行计算
     */
    @Builder.Default
    private Boolean enableParallelCalculation = true;
    
    /**
     * 是否启用缓存
     */
    @Builder.Default
    private Boolean enableCaching = true;
    
    /**
     * 最大重试次数
     */
    @Builder.Default
    private Integer maxRetryAttempts = 3;
    
    /**
     * 超时时间（秒）
     */
    @Builder.Default
    private Integer timeoutSeconds = 300;
    
    /**
     * 批处理大小
     */
    @Builder.Default
    private Integer batchSize = 100;
    
    /**
     * 是否启用详细日志
     */
    @Builder.Default
    private Boolean enableDetailedLogging = false;
} 