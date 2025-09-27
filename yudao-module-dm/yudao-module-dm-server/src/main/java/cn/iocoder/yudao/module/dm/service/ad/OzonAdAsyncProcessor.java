package cn.iocoder.yudao.module.dm.service.ad;

import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdSyncTaskDO;
import cn.iocoder.yudao.module.dm.enums.OzonAdSyncTaskStatusEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.ReportAdService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad.AdReportCampaignDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad.AdReportSkuDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonAdHttpUtil;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Ozon广告异步处理器
 * 负责异步处理广告同步任务
 *
 * @author Jax
 */
@Slf4j
@Component
public class OzonAdAsyncProcessor {

    @Resource
    private OzonAdSyncTaskService ozonAdSyncTaskService;

    @Resource
    private OzonAdHttpUtil ozonAdHttpUtil;

    @Resource
    private ReportAdService reportAdService;

    @Resource
    private OzonAdCampaignsService ozonAdCampaignsService;

    @Resource
    private OzonShopMappingService ozonShopMappingService;

    /**
     * 同步处理同步任务（用于串行执行同一门店的任务）
     * 根据任务状态处理不同阶段的流程
     *
     * @param taskId 任务ID
     */
    public void processTaskSync(Long taskId) {
        log.info("[processTaskSync] 开始同步处理任务，taskId={}", taskId);
        
        OzonAdSyncTaskDO task;
        try {
            // 获取任务信息
            task = ozonAdSyncTaskService.getTask(taskId);
            if (task == null) {
                log.warn("[processTaskSync] 任务不存在，taskId={}", taskId);
                return;
            }

            // 根据任务状态处理不同阶段的任务
            if (OzonAdSyncTaskStatusEnum.isPending(task.getStatus())) {
                // 阶段1：创建报告
                processCreateReport(task);
            } else if (OzonAdSyncTaskStatusEnum.isProcessing(task.getStatus())) {
                // 阶段2：检查报告状态并处理数据
                processCheckReportAndData(task);
            } else {
                log.warn("[processTaskSync] 任务状态不需要处理，taskId={}, status={}", taskId, task.getStatus());
            }

        } catch (Exception e) {
            log.error("[processTaskSync] 处理任务异常，taskId={}", taskId, e);
        }
    }



    /**
     * 处理报告创建阶段
     */
    private void processCreateReport(OzonAdSyncTaskDO task) {
        log.info("[processCreateReport] 开始创建报告，taskId={}, clientId={}", task.getId(), task.getClientId());
        
        try {
            // 更新任务状态为处理中
            ozonAdSyncTaskService.updateTaskStatus(task.getId(), OzonAdSyncTaskStatusEnum.PROCESSING.getStatus(), null);

            // 调用Ozon API创建报告
            String reportUuid = createOzonReport(task);
            
            if (StringUtils.isBlank(reportUuid)) {
                handleTaskError(task.getId(), "创建Ozon报告失败：返回的reportUuid为空");
                return;
            }
            
            // 更新任务报告UUID
            ozonAdSyncTaskService.updateTaskReportUuid(task.getId(), reportUuid);
            log.info("[processCreateReport] 成功创建Ozon报告，taskId={}, reportUuid={}", task.getId(), reportUuid);

        } catch (Exception e) {
            log.error("[processCreateReport] 创建报告异常，taskId={}", task.getId(), e);
            handleTaskError(task.getId(), "创建报告异常：" + e.getMessage());
        }
    }

    /**
     * 处理报告状态检查和数据处理阶段
     */
    private void processCheckReportAndData(OzonAdSyncTaskDO task) {
        log.info("[processCheckReportAndData] 开始检查报告状态，taskId={}, reportUuid={}", 
                task.getId(), task.getReportUuid());
        
        try {
            // 检查报告状态
            String reportStatus = checkOzonReportStatus(task);
            
            if ("SUCCESS".equalsIgnoreCase(reportStatus)) {
                // 报告已完成，下载并处理数据
                processReportData(task);
            } else if ("FAILED".equalsIgnoreCase(reportStatus)) {
                handleTaskError(task.getId(), "Ozon报告生成失败");
            } else {
                // 报告还在生成中，等待下次定时任务重试
                log.info("[processCheckReportAndData] 报告还在生成中，taskId={}, reportUuid={}, 等待下次重试", 
                        task.getId(), task.getReportUuid());
                // 不需要特殊处理，等待下次定时任务扫描
            }

        } catch (Exception e) {
            log.error("[processCheckReportAndData] 检查报告状态异常，taskId={}", task.getId(), e);
        }
    }

    /**
     * 创建Ozon报告
     */
    private String createOzonReport(OzonAdSyncTaskDO task) {
        try {
            log.info("[createOzonReport] 调用Ozon API创建报告，taskId={}, clientId={}, beginDate={}, endDate={}, campaignIds={}", 
                    task.getId(), task.getClientId(), task.getBeginDate(), task.getEndDate(), task.getCampaignIds());
            
            // task中的beginDate和endDate是LocalDate类型，需要格式化为字符串
            String beginDateStr = task.getBeginDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDateStr = task.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // 解析任务中的campaignIds
            List<String> campaignIds = task.getCampaignIds();
            
            // 调用真实的Ozon API创建广告统计报告
            String reportUuid = reportAdService.getAdStatistics(task.getClientId(), campaignIds, beginDateStr, endDateStr);
            
            if (StringUtils.isNotBlank(reportUuid)) {
                // 解析返回的JSON，获取UUID
                JSONObject jsonObject = JSONObject.parseObject(reportUuid);
                String uuid = jsonObject.getString("UUID");
                
                if (StringUtils.isNotBlank(uuid)) {
                    log.info("[createOzonReport] 成功创建Ozon报告，taskId={}, reportUuid={}", task.getId(), uuid);
                    return uuid;
                } else {
                    log.error("[createOzonReport] 创建报告返回的UUID为空，taskId={}, response={}", task.getId(), reportUuid);
                    throw new RuntimeException("创建报告返回的UUID为空");
                }
            } else {
                log.error("[createOzonReport] 创建报告失败，返回结果为空，taskId={}", task.getId());
                throw new RuntimeException("创建报告失败，返回结果为空");
            }
            
        } catch (Exception e) {
            log.error("[createOzonReport] 创建Ozon报告异常，taskId={}", task.getId(), e);
            throw new RuntimeException("创建Ozon报告失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查Ozon报告状态
     */
    private String checkOzonReportStatus(OzonAdSyncTaskDO task) {
        try {
            // 调用真实的Ozon API检查报告状态
            String response = ozonAdHttpUtil.getByPathVariables(task.getClientId(), OzonConfig.OZON_AD_REPORT_STATE, task.getReportUuid());
            
            if (StringUtils.isNotBlank(response)) {
                JSONObject jsonObject = JSONObject.parseObject(response);
                String state = jsonObject.getString("state");
                
                if ("OK".equals(state)) {
                    log.info("[checkOzonReportStatus] 报告已准备就绪，taskId={}, reportUuid={}", task.getId(), task.getReportUuid());
                    return "SUCCESS";
                } else if ("ERROR".equals(state)) {
                    log.error("[checkOzonReportStatus] 报告生成失败，taskId={}, reportUuid={}, state={}", task.getId(), task.getReportUuid(), state);
                    return "FAILED";
                } else {
                    log.info("[checkOzonReportStatus] 报告还在生成中，taskId={}, reportUuid={}, state={}", task.getId(), task.getReportUuid(), state);
                    return "PROCESSING";
                }
            } else {
                log.warn("[checkOzonReportStatus] 检查报告状态返回空响应，taskId={}, reportUuid={}", task.getId(), task.getReportUuid());
                return "PROCESSING";
            }
            
        } catch (Exception e) {
            log.error("[checkOzonReportStatus] 检查Ozon报告状态异常，taskId={}", task.getId(), e);
            throw new RuntimeException("检查Ozon报告状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理报告数据
     */
    private void processReportData(OzonAdSyncTaskDO task) {
        try {
            log.info("[processReportData] 开始处理报告数据，taskId={}, reportUuid={}", 
                    task.getId(), task.getReportUuid());
            
            // 调用真实的Ozon API获取报告详细数据
            Map<String, AdReportCampaignDTO> adReportDetail = reportAdService.getAdReportDetail(task.getClientId(), task.getReportUuid());
            
            if (adReportDetail == null || adReportDetail.isEmpty()) {
                log.warn("[processReportData] 报告数据为空，taskId={}, reportUuid={}", task.getId(), task.getReportUuid());
                // 即使数据为空也标记任务完成
                ozonAdSyncTaskService.completeTask(task.getId(), 0);
                return;
            }
            
            // 处理报告数据并保存到数据库
            int processedCount = processAdReportDetail(task, adReportDetail);
            
            // 完成任务
            ozonAdSyncTaskService.completeTask(task.getId(), processedCount);
            
            log.info("[processReportData] 报告数据处理完成，taskId={}, processedCount={}", 
                    task.getId(), processedCount);
            
        } catch (Exception e) {
            log.error("[processReportData] 处理报告数据异常，taskId={}", task.getId(), e);
            handleTaskError(task.getId(), "处理报告数据异常：" + e.getMessage());
        }
    }

    /**
     * 处理广告报告详细数据并保存到数据库
     * 类似于ReportAdService#syncAdCampaignsItem的实现
     */
    private int processAdReportDetail(OzonAdSyncTaskDO task, Map<String, AdReportCampaignDTO> adReportDetail) {
        
        List<OzonAdCampaignsItemDO> insertList = new ArrayList<>();
        List<OzonAdCampaignsItemDO> updateList = new ArrayList<>();
        
        // 获取租户ID
        OzonShopMappingDO shopMapping = ozonShopMappingService.getOzonShopMappingByClientId(task.getClientId());
        Long tenantId = shopMapping != null ? shopMapping.getTenantId() : 0L;
        
        final int[] processedCount = {0};
        
        adReportDetail.forEach((campaignId, adReportCampaignDTO) -> {
            if (adReportCampaignDTO != null && adReportCampaignDTO.getReport() != null && 
                adReportCampaignDTO.getReport().getRows() != null) {
                
                List<AdReportSkuDTO> rows = adReportCampaignDTO.getReport().getRows();
                for (AdReportSkuDTO row : rows) {
                    OzonAdCampaignsItemDO adCampaignsItemDO = new OzonAdCampaignsItemDO();

                    // 搜索广告没有click这些值
                    if (StringUtils.isBlank(row.getOrderId())) {
                        adCampaignsItemDO.setViews(row.getViews() == null ? 0 : Integer.parseInt(row.getViews()));
                        adCampaignsItemDO.setClicks(row.getClicks() == null ? 0 : Integer.parseInt(row.getClicks()));
                        adCampaignsItemDO.setCr(row.getCr() == null ? BigDecimal.ZERO : new BigDecimal(row.getCr()));
                        adCampaignsItemDO.setOrders(row.getOrders() == null ? 0 : Integer.parseInt(row.getOrders()));
                        adCampaignsItemDO.setAvgBid(row.getAvgBid() == null ? BigDecimal.ZERO : new BigDecimal(row.getAvgBid()));
                        adCampaignsItemDO.setOrdersMoney(new BigDecimal(row.getOrdersMoney()));
                    } else {
                        adCampaignsItemDO.setOrdersMoney(StringUtils.isBlank(row.getCost()) ? BigDecimal.ZERO : new BigDecimal(row.getCost()));
                        adCampaignsItemDO.setOrderId(row.getOrderId());
                        adCampaignsItemDO.setOrders(1);
                        adCampaignsItemDO.setAvgBid(StringUtils.isBlank(row.getBidValue()) ? BigDecimal.ZERO : new BigDecimal(row.getBidValue()));
                    }
                    
                    adCampaignsItemDO.setClientId(task.getClientId());
                    adCampaignsItemDO.setPlatformSkuId(row.getSku());
                    adCampaignsItemDO.setMoneySpent(new BigDecimal(row.getMoneySpent()));
                    adCampaignsItemDO.setPrice(new BigDecimal(row.getPrice()));
                    
                    // 解析日期格式
                    java.time.LocalDate localDate = java.time.LocalDate.parse(row.getDate(), 
                            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    adCampaignsItemDO.setDate(localDate);
                    adCampaignsItemDO.setCampaignId(campaignId);
                    adCampaignsItemDO.setTenantId(tenantId);
                    
                    // 检查是否已存在记录
                    OzonAdCampaignsItemDO existItemDO;
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(row.getOrderId())) {
                        existItemDO = ozonAdCampaignsService.getOzonAdCampaignItemByOrderId(
                                campaignId, row.getSku(), row.getOrderId(), localDate);
                    } else {
                        existItemDO = ozonAdCampaignsService.getOzonAdCampaignItemByCampaignId(
                                campaignId, row.getSku(), localDate);
                    }
                    
                    if (existItemDO == null) {
                        insertList.add(adCampaignsItemDO);
                    } else {
                        adCampaignsItemDO.setId(existItemDO.getId());
                        updateList.add(adCampaignsItemDO);
                    }
                    processedCount[0]++;
                }
            }
        });
        
        // 批量插入和更新数据
        if (!insertList.isEmpty()) {
            ozonAdCampaignsService.createOzonAdCampaignsItem(insertList);
        }
        
        if (!updateList.isEmpty()) {
            ozonAdCampaignsService.updateOzonAdCampaignsItem(updateList);
        }
        
        return processedCount[0];
    }



    /**
     * 处理任务错误
     */
    private void handleTaskError(Long taskId, String errorMessage) {
        try {
            OzonAdSyncTaskDO task = ozonAdSyncTaskService.getTask(taskId);
            if (task == null) {
                log.warn("[handleTaskError] 任务不存在，taskId={}", taskId);
                return;
            }

            // 增加重试次数
            ozonAdSyncTaskService.incrementRetryCount(taskId, errorMessage);
            
            log.warn("[handleTaskError] 任务处理失败，taskId={}, retryCount={}, error={}",
                    taskId, task.getRetryCount() + 1, errorMessage);
            
        } catch (Exception e) {
            log.error("[handleTaskError] 处理任务错误异常，taskId={}", taskId, e);
        }
    }
}