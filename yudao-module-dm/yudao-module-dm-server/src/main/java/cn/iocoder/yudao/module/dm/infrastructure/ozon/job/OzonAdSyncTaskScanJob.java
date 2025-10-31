package cn.iocoder.yudao.module.dm.infrastructure.ozon.job;

import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdSyncTaskDO;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdAsyncProcessor;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdSyncTaskService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ozon广告同步任务扫描Job
 * 负责扫描并处理未执行成功的任务
 *
 * @author Jax
 */
@Slf4j
@Component
public class OzonAdSyncTaskScanJob {

    @Resource
    private OzonAdSyncTaskService ozonAdSyncTaskService;

    @Resource
    private OzonAdAsyncProcessor ozonAdAsyncProcessor;

    /**
     * 每次扫描的任务数量限制
     */
    private static final Integer SCAN_LIMIT = 50;

    @XxlJob("ozonAdSyncTaskScanJob")
    @TenantJob
    public String execute(String param) throws Exception {
        log.info("[OzonAdSyncTaskScanJob] 开始执行任务扫描，param={}", param);
        
        try {
            // 1. 获取需要重试的任务
            List<OzonAdSyncTaskDO> pendingTasks = ozonAdSyncTaskService.getPendingTasksForRetry(SCAN_LIMIT);
            
            if (CollectionUtils.isEmpty(pendingTasks)) {
                log.warn("[OzonAdSyncTaskScanJob] 没有需要处理的任务");
                return "扫描完成，无待处理任务";
            }
            
            log.info("[OzonAdSyncTaskScanJob] 发现 {} 个待处理任务", pendingTasks.size());
            
            // 2. 按门店分组处理任务，同一门店的任务串行执行，不同门店的任务并行执行
            Map<String, List<OzonAdSyncTaskDO>> groupedTasks = pendingTasks.stream()
                    .collect(Collectors.groupingBy(OzonAdSyncTaskDO::getClientId));
            
            log.info("[OzonAdSyncTaskScanJob] 按门店分组，共涉及 {} 个门店", groupedTasks.size());
            
            // 3. 处理不同门店的任务组
            int totalTasks = 0;
            int totalSuccess = 0;
            int totalError = 0;
            
            for (Map.Entry<String, List<OzonAdSyncTaskDO>> entry : groupedTasks.entrySet()) {
                String clientId = entry.getKey();
                List<OzonAdSyncTaskDO> storeTasks = entry.getValue();
                
                totalTasks += storeTasks.size();
                
                // 每个门店的任务完全串行执行
                int storeSuccess = 0;
                int storeError = 0;
                
                for (OzonAdSyncTaskDO task : storeTasks) {
                    try {
                        log.info("[OzonAdSyncTaskScanJob] 开始处理任务，taskId={}, tenantId={}, clientId={}, status={}",
                                task.getId(), task.getTenantId(), task.getClientId(), task.getStatus());
                        
                        // 同步调用处理器处理任务，确保同一门店的任务串行执行
                        ozonAdAsyncProcessor.processTaskSync(task.getId());
                        storeSuccess++;
                        totalSuccess++;
                        rateLimitControl();
                    } catch (Exception e) {
                        storeError++;
                        totalError++;
                        log.error("[OzonAdSyncTaskScanJob] 处理任务失败，taskId={}, error={}", task.getId(), e.getMessage(), e);
                    }
                }
                
                log.info("[OzonAdSyncTaskScanJob] 门店 {} 处理完成，共 {} 个任务，成功 {} 个，失败 {} 个", 
                        clientId, storeTasks.size(), storeSuccess, storeError);
            }
            
            return String.format("扫描完成，共涉及 %d 个门店，总计 %d 个任务，成功 %d 个，失败 %d 个",
                    groupedTasks.size(), totalTasks, totalSuccess, totalError);
            
        } catch (Exception e) {
            log.error("[OzonAdSyncTaskScanJob] 任务扫描执行失败，param={}, error={}", param, e.getMessage(), e);
            throw e;
        }
    }

    private synchronized void rateLimitControl() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[rateLimitControl] 线程被中断", e);
        }
    }
}