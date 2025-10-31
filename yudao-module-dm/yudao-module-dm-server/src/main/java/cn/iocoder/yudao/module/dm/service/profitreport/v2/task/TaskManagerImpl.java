package cn.iocoder.yudao.module.dm.service.profitreport.v2.task;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitCalculationRequestVO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.ProfitCalculationResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.TaskPhase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 任务管理器实现
 * 负责任务生命周期管理
 *
 * @author Jax
 */
@Component
@Slf4j
public class TaskManagerImpl implements TaskManager {

    /**
     * 内存中的任务状态映射
     */
    private final ConcurrentMap<String, TaskState> taskStates = new ConcurrentHashMap<>();
    
    @Override
    public void initializeTask(String taskId, ProfitCalculationRequestVO request) {
        // 只取第一个clientId
        String clientId = (request.getClientIds() != null && request.getClientIds().length > 0) 
                ? request.getClientIds()[0] : null;
        
        TaskState taskState = TaskState.builder()
                .taskId(taskId)
                .clientId(clientId)
                .phase(TaskPhase.INITIALIZED)
                .progress(0)
                .startTime(LocalDateTime.now())
                .build();
                
        taskStates.put(taskId, taskState);
        
        log.info("任务初始化完成: taskId={}, clientId={}", taskId, clientId);
    }
    
    @Override
    public void updateProgress(String taskId, TaskPhase phase, Integer progress) {
        TaskState taskState = taskStates.get(taskId);
        if (taskState != null) {
            taskState.setPhase(phase);
            taskState.setProgress(progress);
            taskState.setUpdateTime(LocalDateTime.now());
            
            log.info("任务进度更新: taskId={}, phase={}, progress={}%", taskId, phase, progress);
        } else {
            log.warn("任务状态不存在: taskId={}", taskId);
        }
    }
    
    @Override
    public void completeTask(String taskId, ProfitCalculationResult result) {
        TaskState taskState = taskStates.get(taskId);
        if (taskState != null) {
            taskState.setPhase(TaskPhase.COMPLETED);
            taskState.setProgress(100);
            taskState.setEndTime(LocalDateTime.now());
            taskState.setResult(result);
            
            log.info("任务完成: taskId={}, 处理记录数={}", taskId, result.getTotalRecords());
        } else {
            log.warn("任务状态不存在: taskId={}", taskId);
        }
    }
    
    @Override
    public void failTask(String taskId, Exception error) {
        TaskState taskState = taskStates.get(taskId);
        if (taskState != null) {
            taskState.setPhase(TaskPhase.FAILED);
            taskState.setEndTime(LocalDateTime.now());
            taskState.setErrorMessage(error.getMessage());
            
            log.error("任务失败: taskId={}, error={}", taskId, error.getMessage(), error);
        } else {
            log.warn("任务状态不存在: taskId={}", taskId);
        }
    }
    
    @Override
    public Boolean cancelTask(String taskId) {
        TaskState taskState = taskStates.get(taskId);
        if (taskState != null && taskState.getPhase() != TaskPhase.COMPLETED && taskState.getPhase() != TaskPhase.FAILED) {
            taskState.setPhase(TaskPhase.CANCELLED);
            taskState.setEndTime(LocalDateTime.now());
            
            log.info("任务已取消: taskId={}", taskId);
            return true;
        }
        
        log.warn("无法取消任务: taskId={}, 当前状态={}", taskId, taskState != null ? taskState.getPhase() : "NOT_FOUND");
        return false;
    }
    
    /**
     * 获取任务状态
     */
    public TaskState getTaskState(String taskId) {
        return taskStates.get(taskId);
    }
    
    /**
     * 清理过期任务状态
     */
    public void cleanupExpiredTasks(int retentionHours) {
        LocalDateTime expiredBefore = LocalDateTime.now().minusHours(retentionHours);
        
        taskStates.entrySet().removeIf(entry -> {
            TaskState state = entry.getValue();
            return state.getEndTime() != null && state.getEndTime().isBefore(expiredBefore);
        });
        
        log.info("清理过期任务状态完成，保留{}小时内的任务", retentionHours);
    }
} 