package cn.iocoder.yudao.module.dm.service.ad;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdSyncTaskExportReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdSyncTaskPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdSyncTaskDO;
import cn.iocoder.yudao.module.dm.dal.mysql.ad.OzonAdSyncTaskMapper;
import cn.iocoder.yudao.module.dm.enums.OzonAdSyncTaskStatusEnum;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.OZON_AD_SYNC_TASK_ERROR;

/**
 * Ozon广告同步任务 Service 实现类
 *
 * @author Jax
 */
@Slf4j
@Service
@Validated
public class OzonAdSyncTaskServiceImpl implements OzonAdSyncTaskService {

    @Resource
    private OzonAdSyncTaskMapper ozonAdSyncTaskMapper;

    /**
     * 默认最大重试次数
     */
    private static final Integer DEFAULT_MAX_RETRY_COUNT = 30;

    /**
     * 默认批量查询限制
     */
    private static final Integer DEFAULT_BATCH_LIMIT = 100;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSyncTask(Long tenantId, String clientId, LocalDate beginDate, 
                               LocalDate endDate, String taskParams, String remark) {
        return createSyncTaskWithCampaigns(tenantId, clientId, beginDate, endDate, null, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSyncTaskWithCampaigns(Long tenantId, String clientId, LocalDate beginDate, 
                                           LocalDate endDate, List<String> campaignIds, String remark) {
        // 参数校验
        validateTaskParams(tenantId, clientId, beginDate, endDate);
        
        // 检查是否存在冲突的任务
//        if (hasConflictTask(tenantId, clientId, beginDate, endDate)) {
//            throw exception(OZON_AD_SYNC_TASK_ERROR,"存在正在进行的同步任务，请稍后再试");
//        }

        // 创建任务
        OzonAdSyncTaskDO task = new OzonAdSyncTaskDO();
        task.setTenantId(tenantId);
        task.setClientId(clientId);
        task.setBeginDate(beginDate);
        task.setEndDate(endDate);
        task.setStatus(OzonAdSyncTaskStatusEnum.PENDING.getStatus());
        task.setRetryCount(0);
        task.setMaxRetryCount(DEFAULT_MAX_RETRY_COUNT);
        task.setRemark(remark);

        // 设置广告活动ID列表
        if (campaignIds != null && !campaignIds.isEmpty()) {
            task.setCampaignIds(campaignIds);
        }

        ozonAdSyncTaskMapper.insert(task);
        
        log.info("[createSyncTaskWithCampaigns] 创建同步任务成功，taskId={}, tenantId={}, clientId={}, beginDate={}, endDate={}, campaignIds={}", 
                task.getId(), tenantId, clientId, beginDate, endDate, campaignIds);
        
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createBatchSyncTasks(Long tenantId, String clientId, LocalDate beginDate, 
                                          LocalDate endDate, List<String> campaignIds, String remark) {
        if (campaignIds == null || campaignIds.isEmpty()) {
            // 如果没有指定广告活动ID，创建一个不带campaignIds的任务
            Long taskId = createSyncTaskWithCampaigns(tenantId, clientId, beginDate, endDate, null, remark);
            return Collections.singletonList(taskId);
        }
        
        // 按每批最多5个广告活动ID进行分批
        List<List<String>> batches = Lists.partition(campaignIds, 10);
        List<Long> taskIds = new ArrayList<>();
        
        for (int i = 0; i < batches.size(); i++) {
            List<String> batch = batches.get(i);
            String batchRemark = batches.size() > 1 ? 
                String.format("%s (批次 %d/%d)", remark, i + 1, batches.size()) : remark;
            
            Long taskId = createSyncTaskWithCampaigns(tenantId, clientId, beginDate, endDate, batch, batchRemark);
            taskIds.add(taskId);
        }
        
        log.info("[createBatchSyncTasks] 批量创建同步任务成功，tenantId={}, clientId={}, beginDate={}, endDate={}, 总广告活动数={}, 创建任务数={}", 
                tenantId, clientId, beginDate, endDate, campaignIds.size(), taskIds.size());
        
        return taskIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskStatus(Long taskId, Integer status, String errorMessage) {
        OzonAdSyncTaskDO task = validateTaskExists(taskId);
        
        OzonAdSyncTaskDO updateTask = new OzonAdSyncTaskDO();
        updateTask.setId(taskId);
        updateTask.setStatus(status);
        
        if (StringUtils.hasText(errorMessage)) {
            updateTask.setErrorMessage(errorMessage);
        }
        
        ozonAdSyncTaskMapper.updateById(updateTask);
        
        log.info("[updateTaskStatus] 更新任务状态成功，taskId={}, oldStatus={}, newStatus={}, errorMessage={}", 
                taskId, task.getStatus(), status, errorMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskReportUuid(Long taskId, String reportUuid) {
        validateTaskExists(taskId);
        
        OzonAdSyncTaskDO updateTask = new OzonAdSyncTaskDO();
        updateTask.setId(taskId);
        updateTask.setReportUuid(reportUuid);
        updateTask.setStatus(OzonAdSyncTaskStatusEnum.PROCESSING.getStatus());
        
        ozonAdSyncTaskMapper.updateById(updateTask);
        
        log.info("[updateTaskReportUuid] 更新任务报告UUID成功，taskId={}, reportUuid={}", taskId, reportUuid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementRetryCount(Long taskId, String errorMessage) {
        OzonAdSyncTaskDO task = validateTaskExists(taskId);
        
        OzonAdSyncTaskDO updateTask = new OzonAdSyncTaskDO();
        updateTask.setId(taskId);
        updateTask.setRetryCount(task.getRetryCount() + 1);
        updateTask.setErrorMessage(errorMessage);
        updateTask.setStatus(OzonAdSyncTaskStatusEnum.PENDING.getStatus());
        
        // 如果超过最大重试次数，标记为失败
        if (updateTask.getRetryCount() >= task.getMaxRetryCount()) {
            updateTask.setStatus(OzonAdSyncTaskStatusEnum.FAILED.getStatus());
        }
        
        ozonAdSyncTaskMapper.updateById(updateTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId, Integer processedCount) {
        validateTaskExists(taskId);
        
        OzonAdSyncTaskDO updateTask = new OzonAdSyncTaskDO();
        updateTask.setId(taskId);
        updateTask.setStatus(OzonAdSyncTaskStatusEnum.COMPLETED.getStatus());

        ozonAdSyncTaskMapper.updateById(updateTask);
        
        log.info("[completeTask] 完成任务成功，taskId={}, processedCount={}", taskId, processedCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long taskId, String reason) {
        validateTaskExists(taskId);
        
        OzonAdSyncTaskDO updateTask = new OzonAdSyncTaskDO();
        updateTask.setId(taskId);
        updateTask.setStatus(OzonAdSyncTaskStatusEnum.FAILED.getStatus());
        updateTask.setErrorMessage(reason);

        ozonAdSyncTaskMapper.updateById(updateTask);
        
        log.info("[cancelTask] 取消任务成功，taskId={}, reason={}", taskId, reason);
    }

    @Override
    public void cancelTask(Long taskId) {
        cancelTask(taskId, "手动取消");
    }

    @Override
    public void resetTaskForRetry(Long taskId) {
        OzonAdSyncTaskDO task = validateTaskExists(taskId);
        if (!OzonAdSyncTaskStatusEnum.isFailed(task.getStatus())) {
            throw exception(OZON_AD_SYNC_TASK_ERROR,"任务状态不允许重试");
        }
        
        OzonAdSyncTaskDO updateTask = new OzonAdSyncTaskDO();
        updateTask.setId(taskId);
        updateTask.setStatus(OzonAdSyncTaskStatusEnum.PENDING.getStatus());
        updateTask.setErrorMessage(null);

        ozonAdSyncTaskMapper.updateById(updateTask);
        
        log.info("[resetTaskForRetry] 重置任务状态成功，taskId={}", taskId);
    }

    @Override
    public long countTasksByStatus(Integer status) {
        if (status == null) {
            return 0;
        }
        
        return ozonAdSyncTaskMapper.selectCount(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .eq(OzonAdSyncTaskDO::getStatus, status));
    }

    @Override
    public int deleteExpiredTasks() {
        // 删除30天前的已完成任务
        LocalDateTime expireTime = LocalDateTime.now().minusDays(30);
        return ozonAdSyncTaskMapper.deleteExpiredTasks(
                Arrays.asList(
                    OzonAdSyncTaskStatusEnum.COMPLETED.getStatus(),
                    OzonAdSyncTaskStatusEnum.FAILED.getStatus(),
                    OzonAdSyncTaskStatusEnum.OBSOLETE.getStatus()
                ),
                expireTime
        );
    }

    @Override
    public Long createTask(Long tenantId, String clientId, LocalDate beginDate, LocalDate endDate, String remark) {
        return createSyncTask(tenantId, clientId, beginDate, endDate, null, remark);
    }

    @Override
    public OzonAdSyncTaskDO getTask(Long taskId) {
        return ozonAdSyncTaskMapper.selectById(taskId);
    }

    @Override
    public OzonAdSyncTaskDO getTaskByReportUuid(String reportUuid) {
        if (!StringUtils.hasText(reportUuid)) {
            return null;
        }
        return ozonAdSyncTaskMapper.selectByReportUuid(reportUuid);
    }

    @Override
    public List<OzonAdSyncTaskDO> getPendingTasks(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = DEFAULT_BATCH_LIMIT;
        }
        return ozonAdSyncTaskMapper.selectPendingTasks(OzonAdSyncTaskStatusEnum.PENDING.getStatus(), limit);
    }

    @Override
    public List<OzonAdSyncTaskDO> getRetryTasks(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = DEFAULT_BATCH_LIMIT;
        }
        return ozonAdSyncTaskMapper.selectRetryTasks(
                OzonAdSyncTaskStatusEnum.FAILED.getStatus(), 
                DEFAULT_MAX_RETRY_COUNT, 
                LocalDateTime.now(), 
                limit
        );
    }

    @Override
    public List<OzonAdSyncTaskDO> getWaitingReportTasks(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = DEFAULT_BATCH_LIMIT;
        }
        return ozonAdSyncTaskMapper.selectWaitingReportTasks(OzonAdSyncTaskStatusEnum.PROCESSING.getStatus(), limit);
    }

    @Override
    public List<OzonAdSyncTaskDO> getRunningTasks(Long tenantId, String clientId) {
        List<Integer> runningStatuses = Arrays.asList(
                OzonAdSyncTaskStatusEnum.PENDING.getStatus(),
                OzonAdSyncTaskStatusEnum.PROCESSING.getStatus()
        );
        return ozonAdSyncTaskMapper.selectRunningTasks(tenantId, clientId, runningStatuses);
    }

    @Override
    public boolean hasConflictTask(Long tenantId, String clientId, LocalDate beginDate, LocalDate endDate) {
        List<OzonAdSyncTaskDO> runningTasks = getRunningTasks(tenantId, clientId);
        
        for (OzonAdSyncTaskDO task : runningTasks) {
            // 检查日期范围是否有重叠
            if (isDateRangeOverlap(beginDate, endDate, task.getBeginDate(), task.getEndDate())) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Long countTasksByTimeRange(Long tenantId, String clientId, LocalDateTime startTime, LocalDateTime endTime) {
        return ozonAdSyncTaskMapper.countByTimeRange(tenantId, clientId, startTime, endTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredTasks(Integer expireDays) {
        if (expireDays == null || expireDays <= 0) {
            expireDays = 30; // 默认30天
        }
        
        LocalDateTime expireTime = LocalDateTime.now().minusDays(expireDays);
        List<Integer> finalStatuses = Arrays.asList(
                OzonAdSyncTaskStatusEnum.COMPLETED.getStatus(),
                OzonAdSyncTaskStatusEnum.FAILED.getStatus(),
                OzonAdSyncTaskStatusEnum.OBSOLETE.getStatus()
        );
        
        int deletedCount = ozonAdSyncTaskMapper.deleteExpiredTasks(finalStatuses, expireTime);
        
        log.info("[cleanExpiredTasks] 清理过期任务完成，expireDays={}, deletedCount={}", expireDays, deletedCount);
        
        return deletedCount;
    }

    @Override
    public PageResult<OzonAdSyncTaskDO> getTaskPage(OzonAdSyncTaskPageReqVO pageReqVO) {
        return ozonAdSyncTaskMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OzonAdSyncTaskDO> getTaskList(OzonAdSyncTaskExportReqVO exportReqVO) {
        return ozonAdSyncTaskMapper.selectList(exportReqVO);
    }

    @Override
    public void validateTaskParams(Long tenantId, String clientId, LocalDate beginDate, LocalDate endDate) {
        if (!StringUtils.hasText(clientId)) {
            throw exception(OZON_AD_SYNC_TASK_ERROR, "客户端ID不能为空");
        }
        if (beginDate == null) {
            throw exception(OZON_AD_SYNC_TASK_ERROR,"开始日期不能为空");
        }
        if (endDate == null) {
            throw exception(OZON_AD_SYNC_TASK_ERROR,"结束日期不能为空");
        }
    }

    @Override
    public List<OzonAdSyncTaskDO> getPendingTasksForRetry(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = DEFAULT_BATCH_LIMIT;
        }
        
        // 查询状态为"待处理"和"处理中"且到达重试时间的任务
        List<Integer> statuses = Arrays.asList(
                OzonAdSyncTaskStatusEnum.PENDING.getStatus(),
                OzonAdSyncTaskStatusEnum.PROCESSING.getStatus()
        );
        
        return ozonAdSyncTaskMapper.selectPendingTasksForRetry(statuses, LocalDateTime.now(), limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskError(Long taskId, String errorMessage) {
        validateTaskExists(taskId);
        
        OzonAdSyncTaskDO updateTask = new OzonAdSyncTaskDO();
        updateTask.setId(taskId);
        updateTask.setErrorMessage(errorMessage);
        
        ozonAdSyncTaskMapper.updateById(updateTask);
        
        log.info("[updateTaskError] 更新任务错误信息成功，taskId={}, errorMessage={}", taskId, errorMessage);
    }

    /**
     * 校验任务是否存在
     */
    private OzonAdSyncTaskDO validateTaskExists(Long taskId) {
        if (taskId == null) {
            throw exception(OZON_AD_SYNC_TASK_ERROR,"任务ID不能为空");
        }
        
        OzonAdSyncTaskDO task = ozonAdSyncTaskMapper.selectById(taskId);
        if (task == null) {
            throw exception(OZON_AD_SYNC_TASK_ERROR,"任务不存在");
        }
        
        return task;
    }

    /**
     * 检查日期范围是否重叠
     */
    private boolean isDateRangeOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

}