package cn.iocoder.yudao.module.dm.dal.mysql.ad;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdSyncTaskExportReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdSyncTaskPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdSyncTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ozon广告同步任务 Mapper
 *
 * @author Jax
 */
@Mapper
public interface OzonAdSyncTaskMapper extends BaseMapperX<OzonAdSyncTaskDO> {

    /**
     * 查询待处理的任务
     *
     * @param status 任务状态
     * @param limit  限制数量
     * @return 任务列表
     */
    default List<OzonAdSyncTaskDO> selectPendingTasks(Integer status, Integer limit) {
        return selectList(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .eq(OzonAdSyncTaskDO::getStatus, status)
                .orderByAsc(OzonAdSyncTaskDO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 查询需要重试的任务
     *
     * @param status        任务状态
     * @param maxRetryCount 最大重试次数
     * @param currentTime   当前时间
     * @param limit         限制数量
     * @return 任务列表
     */
    default List<OzonAdSyncTaskDO> selectRetryTasks(Integer status, Integer maxRetryCount, 
                                                     LocalDateTime currentTime, Integer limit) {
        return selectList(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .eq(OzonAdSyncTaskDO::getStatus, status)
                .lt(OzonAdSyncTaskDO::getRetryCount, maxRetryCount)
                .last("LIMIT " + limit));
    }

    /**
     * 查询等待报告的任务
     *
     * @param status 任务状态
     * @param limit  限制数量
     * @return 任务列表
     */
    default List<OzonAdSyncTaskDO> selectWaitingReportTasks(Integer status, Integer limit) {
        return selectList(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .eq(OzonAdSyncTaskDO::getStatus, status)
                .isNotNull(OzonAdSyncTaskDO::getReportUuid)
                .orderByAsc(OzonAdSyncTaskDO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 根据租户ID和客户端ID查询正在进行的任务
     *
     * @param tenantId 租户ID
     * @param clientId 客户端ID
     * @param statuses 任务状态列表
     * @return 任务列表
     */
    default List<OzonAdSyncTaskDO> selectRunningTasks(Long tenantId, String clientId, List<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .eq(OzonAdSyncTaskDO::getTenantId, tenantId)
                .eq(OzonAdSyncTaskDO::getClientId, clientId)
                .in(OzonAdSyncTaskDO::getStatus, statuses)
                .orderByDesc(OzonAdSyncTaskDO::getCreateTime));
    }

    /**
     * 根据报告UUID查询任务
     *
     * @param reportUuid 报告UUID
     * @return 任务
     */
    default OzonAdSyncTaskDO selectByReportUuid(String reportUuid) {
        return selectOne(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .eq(OzonAdSyncTaskDO::getReportUuid, reportUuid));
    }

    /**
     * 统计指定时间范围内的任务数量
     *
     * @param tenantId  租户ID
     * @param clientId  客户端ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 任务数量
     */
    default Long countByTimeRange(Long tenantId, String clientId, LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .eq(OzonAdSyncTaskDO::getTenantId, tenantId)
                .eqIfPresent(OzonAdSyncTaskDO::getClientId, clientId)
                .ge(OzonAdSyncTaskDO::getCreateTime, startTime)
                .le(OzonAdSyncTaskDO::getCreateTime, endTime));
    }

    /**
     * 清理过期的已完成任务
     *
     * @param statuses   终态状态列表
     * @param expireTime 过期时间
     * @return 删除数量
     */
    default int deleteExpiredTasks(List<Integer> statuses, LocalDateTime expireTime) {
        return delete(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .in(OzonAdSyncTaskDO::getStatus, statuses));
    }

    /**
     * 查询需要重试的任务（用于任务扫描Job）
     * 查询状态为"待处理"和"处理中"且到达重试时间的任务
     * 增强：对相同campaign_ids和status的数据进行去重，只保留最新的数据
     *
     * @param statuses    任务状态列表
     * @param currentTime 当前时间
     * @param limit       限制数量
     * @return 任务列表
     */
    default List<OzonAdSyncTaskDO> selectPendingTasksForRetry(List<Integer> statuses, 
                                                               LocalDateTime currentTime, Integer limit) {
        // 先查询所有符合条件的任务，按创建时间倒序排列（最新的在前）
        List<OzonAdSyncTaskDO> allTasks = selectList(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .in(OzonAdSyncTaskDO::getStatus, statuses)
                .orderByDesc(OzonAdSyncTaskDO::getCreateTime));
        
        // 使用Java Stream进行去重处理，保留最新的数据，并将旧数据标记为作废
        Map<String, OzonAdSyncTaskDO> uniqueTasksMap = new LinkedHashMap<>();
        List<Long> obsoleteTaskIds = new ArrayList<>();
        
        for (OzonAdSyncTaskDO task : allTasks) {
            // 构建唯一键：campaign_ids + status
            String campaignIdsStr = task.getCampaignIds() != null ? 
                String.join(",", task.getCampaignIds()) : "";
            String uniqueKey = campaignIdsStr + "_" + task.getStatus();
            
            // 如果该键不存在，保留当前任务
            if (!uniqueTasksMap.containsKey(uniqueKey)) {
                uniqueTasksMap.put(uniqueKey, task);
            } else {
                // 如果该键已存在，将当前任务（较旧的）标记为作废
                obsoleteTaskIds.add(task.getId());
            }
        }
        
        // 批量更新旧数据状态为作废
        if (!obsoleteTaskIds.isEmpty()) {
            OzonAdSyncTaskDO updateEntity = new OzonAdSyncTaskDO();
            updateEntity.setStatus(40); // 40为作废状态
            update(updateEntity, new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                    .in(OzonAdSyncTaskDO::getId, obsoleteTaskIds));
        }
        
        // 转换为列表并按创建时间升序排列，然后限制数量
        return uniqueTasksMap.values().stream()
                .sorted(Comparator.comparing(OzonAdSyncTaskDO::getCreateTime))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询任务
     */
    default PageResult<OzonAdSyncTaskDO> selectPage(OzonAdSyncTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .eqIfPresent(OzonAdSyncTaskDO::getTenantId, reqVO.getTenantId())
                .eqIfPresent(OzonAdSyncTaskDO::getClientId, reqVO.getClientId())
                .eqIfPresent(OzonAdSyncTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OzonAdSyncTaskDO::getReportUuid, reqVO.getReportUuid())
                .betweenIfPresent(OzonAdSyncTaskDO::getBeginDate, reqVO.getBeginDate())
                .betweenIfPresent(OzonAdSyncTaskDO::getEndDate, reqVO.getEndDate())
                .betweenIfPresent(OzonAdSyncTaskDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(OzonAdSyncTaskDO::getRetryCount, reqVO.getRetryCount())
                .likeIfPresent(OzonAdSyncTaskDO::getErrorMessage, reqVO.getErrorMessage())
                .likeIfPresent(OzonAdSyncTaskDO::getRemark, reqVO.getRemark())
                .orderByDesc(OzonAdSyncTaskDO::getId));
    }

    /**
     * 导出查询任务
     */
    default List<OzonAdSyncTaskDO> selectList(OzonAdSyncTaskExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<OzonAdSyncTaskDO>()
                .eqIfPresent(OzonAdSyncTaskDO::getTenantId, reqVO.getTenantId())
                .eqIfPresent(OzonAdSyncTaskDO::getClientId, reqVO.getClientId())
                .eqIfPresent(OzonAdSyncTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OzonAdSyncTaskDO::getReportUuid, reqVO.getReportUuid())
                .betweenIfPresent(OzonAdSyncTaskDO::getBeginDate, reqVO.getBeginDate())
                .betweenIfPresent(OzonAdSyncTaskDO::getEndDate, reqVO.getEndDate())
                .betweenIfPresent(OzonAdSyncTaskDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(OzonAdSyncTaskDO::getRetryCount, reqVO.getRetryCount())
                .likeIfPresent(OzonAdSyncTaskDO::getErrorMessage, reqVO.getErrorMessage())
                .likeIfPresent(OzonAdSyncTaskDO::getRemark, reqVO.getRemark())
                .orderByDesc(OzonAdSyncTaskDO::getId));
    }

}