package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import lombok.Builder;
import lombok.Data;

/**
 * 聚合配置
 *
 * @author Jax
 */
@Data
@Builder
public class AggregationConfig {
    
    /**
     * 聚合维度：SKU、CLIENT、PRODUCT
     */
    private String dimension;
    
    /**
     * 时间类型：DAY、WEEK、MONTH
     */
    private String timeType;
    
    /**
     * 是否启用缓存
     */
    @Builder.Default
    private Boolean enableCache = true;
    
    /**
     * 批处理大小
     */
    @Builder.Default
    private Integer batchSize = 1000;
    
    /**
     * 是否并行处理
     */
    @Builder.Default
    private Boolean enableParallel = true;
    
    /**
     * 数据精度（小数位数）
     */
    @Builder.Default
    private Integer precision = 4;
} 