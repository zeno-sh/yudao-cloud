package cn.iocoder.yudao.module.dm.service.profitreport.v2.task;

import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.ProfitCalculationResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.TaskPhase;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务状态
 *
 * @author Jax
 */
@Data
@Builder
public class TaskState {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 任务阶段
     */
    private TaskPhase phase;
    
    /**
     * 进度百分比
     */
    private Integer progress;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 计算结果
     */
    private ProfitCalculationResult result;
} 