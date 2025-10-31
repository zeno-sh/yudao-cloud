package cn.iocoder.yudao.module.dm.service.ad;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdSyncTaskExportReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdSyncTaskPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdSyncTaskDO;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ozon广告同步任务 Service 接口
 *
 * @author Jax
 */
public interface OzonAdSyncTaskService {

    /**
     * 创建同步任务
     *
     * @param tenantId  租户ID
     * @param clientId  客户端ID
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @param remark    备注
     * @return 任务ID
     */
    Long createTask(Long tenantId, String clientId, LocalDate beginDate, 
                    LocalDate endDate, String remark);

    /**
     * 创建同步任务（完整版本）
     *
     * @param tenantId  租户ID
     * @param clientId  客户端ID
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @param taskParams 任务参数（JSON格式）
     * @param remark    备注
     * @return 任务ID
     */
    Long createSyncTask(Long tenantId, String clientId, LocalDate beginDate, 
                        LocalDate endDate, String taskParams, String remark);

    /**
     * 创建同步任务（带广告活动ID）
     *
     * @param tenantId    租户ID
     * @param clientId    客户端ID
     * @param beginDate   开始日期
     * @param endDate     结束日期
     * @param campaignIds 广告活动ID列表
     * @param remark      备注
     * @return 任务ID
     */
    Long createSyncTaskWithCampaigns(Long tenantId, String clientId, LocalDate beginDate, 
                                     LocalDate endDate, List<String> campaignIds, String remark);

    /**
     * 批量创建同步任务（按广告活动ID分批）
     *
     * @param tenantId    租户ID
     * @param clientId    客户端ID
     * @param beginDate   开始日期
     * @param endDate     结束日期
     * @param campaignIds 广告活动ID列表
     * @param remark      备注
     * @return 创建的任务ID列表
     */
    List<Long> createBatchSyncTasks(Long tenantId, String clientId, LocalDate beginDate, 
                                    LocalDate endDate, List<String> campaignIds, String remark);

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态
     * @param errorMessage 错误信息（可选）
     */
    void updateTaskStatus(Long taskId, Integer status, String errorMessage);

    /**
     * 更新任务报告UUID
     *
     * @param taskId     任务ID
     * @param reportUuid 报告UUID
     */
    void updateTaskReportUuid(Long taskId, String reportUuid);

    /**
     * 增加重试次数
     *
     * @param taskId        任务ID
     * @param errorMessage  错误信息
     */
    void incrementRetryCount(Long taskId, String errorMessage);

    /**
     * 完成任务
     *
     * @param taskId         任务ID
     * @param processedCount 处理记录数
     */
    void completeTask(Long taskId, Integer processedCount);

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @param reason 取消原因
     */
    void cancelTask(Long taskId, String reason);

    /**
     * 取消任务（简化版本）
     *
     * @param taskId 任务ID
     */
    void cancelTask(Long taskId);

    /**
     * 重置任务以便重试
     *
     * @param taskId 任务ID
     */
    void resetTaskForRetry(Long taskId);

    /**
     * 根据状态统计任务数量
     *
     * @param status 任务状态
     * @return 任务数量
     */
    long countTasksByStatus(Integer status);

    /**
     * 删除过期任务
     *
     * @return 删除数量
     */
    int deleteExpiredTasks();

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    OzonAdSyncTaskDO getTask(Long taskId);

    /**
     * 根据报告UUID获取任务
     *
     * @param reportUuid 报告UUID
     * @return 任务信息
     */
    OzonAdSyncTaskDO getTaskByReportUuid(String reportUuid);

    /**
     * 获取待处理的任务
     *
     * @param limit 限制数量
     * @return 任务列表
     */
    List<OzonAdSyncTaskDO> getPendingTasks(Integer limit);

    /**
     * 获取需要重试的任务
     *
     * @param limit 限制数量
     * @return 任务列表
     */
    List<OzonAdSyncTaskDO> getRetryTasks(Integer limit);

    /**
     * 获取等待报告的任务
     *
     * @param limit 限制数量
     * @return 任务列表
     */
    List<OzonAdSyncTaskDO> getWaitingReportTasks(Integer limit);

    /**
     * 获取正在进行的任务
     *
     * @param tenantId 租户ID
     * @param clientId 客户端ID
     * @return 任务列表
     */
    List<OzonAdSyncTaskDO> getRunningTasks(Long tenantId, String clientId);

    /**
     * 检查是否存在冲突的任务
     *
     * @param tenantId  租户ID
     * @param clientId  客户端ID
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return 是否存在冲突
     */
    boolean hasConflictTask(Long tenantId, String clientId, LocalDate beginDate, LocalDate endDate);

    /**
     * 统计指定时间范围内的任务数量
     *
     * @param tenantId  租户ID
     * @param clientId  客户端ID（可选）
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 任务数量
     */
    Long countTasksByTimeRange(Long tenantId, String clientId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期的已完成任务
     *
     * @param expireDays 过期天数
     * @return 清理数量
     */
    int cleanExpiredTasks(Integer expireDays);

    /**
     * 验证任务参数
     *
     * @param tenantId  租户ID
     * @param clientId  客户端ID
     * @param beginDate 开始日期
     * @param endDate   结束日期
     */
    void validateTaskParams(Long tenantId, String clientId, LocalDate beginDate, LocalDate endDate);

    /**
     * 获得任务分页
     */
    PageResult<OzonAdSyncTaskDO> getTaskPage(OzonAdSyncTaskPageReqVO pageReqVO);

    /**
     * 获得任务列表，用于导出
     */
    List<OzonAdSyncTaskDO> getTaskList(OzonAdSyncTaskExportReqVO exportReqVO);

    /**
     * 获取需要重试的任务（用于任务扫描Job）
     * 查询状态为"待处理"和"处理中"且到达重试时间的任务
     *
     * @param limit 限制数量
     * @return 任务列表
     */
    List<OzonAdSyncTaskDO> getPendingTasksForRetry(Integer limit);

    /**
     * 更新任务错误信息
     *
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    void updateTaskError(Long taskId, String errorMessage);

}