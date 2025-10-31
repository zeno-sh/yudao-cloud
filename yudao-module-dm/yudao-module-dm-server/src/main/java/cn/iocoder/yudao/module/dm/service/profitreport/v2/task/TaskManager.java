package cn.iocoder.yudao.module.dm.service.profitreport.v2.task;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitCalculationRequestVO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.ProfitCalculationResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.TaskPhase;

/**
 * 任务管理器接口
 * 负责任务生命周期管理
 *
 * @author Jax
 */
public interface TaskManager {
    
    /**
     * 初始化任务
     *
     * @param taskId 任务ID
     * @param request 计算请求
     */
    void initializeTask(String taskId, ProfitCalculationRequestVO request);
    
    /**
     * 更新任务进度
     *
     * @param taskId 任务ID
     * @param phase 任务阶段
     * @param progress 进度百分比
     */
    void updateProgress(String taskId, TaskPhase phase, Integer progress);
    
    /**
     * 完成任务
     *
     * @param taskId 任务ID
     * @param result 最终结果
     */
    void completeTask(String taskId, ProfitCalculationResult result);
    
    /**
     * 任务失败
     *
     * @param taskId 任务ID
     * @param error 错误信息
     */
    void failTask(String taskId, Exception error);
    
    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    Boolean cancelTask(String taskId);
} 