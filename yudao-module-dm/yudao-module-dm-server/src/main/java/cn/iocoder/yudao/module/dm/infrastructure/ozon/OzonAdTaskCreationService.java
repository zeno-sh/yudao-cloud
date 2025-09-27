package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.hutool.core.date.DatePattern;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdCampaignsSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad.AdDailyDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad.AdDailyItemDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonAdHttpUtil;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdCampaignsService;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdSyncTaskService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.OZON_SHOP_MAPPING_NOT_EXISTS;

/**
 * Ozon广告任务创建服务
 * 负责抽象Job和Controller中创建Ozon广告同步任务的通用逻辑
 *
 * @author Jax
 */
@Service
@Slf4j
public class OzonAdTaskCreationService {

    @Resource
    private OzonShopMappingService ozonShopMappingService;
    
    @Resource
    private OzonAdSyncTaskService ozonAdSyncTaskService;
    
    @Resource
    private OzonAdCampaignsService ozonAdCampaignsService;

    @Resource
    private OzonAdHttpUtil ozonAdHttpUtil;

    /**
     * 为所有Ozon店铺创建广告同步任务
     *
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @param remark    备注
     * @return 任务创建结果
     */
    public TaskCreationResult createTasksForAllShops(LocalDate beginDate, LocalDate endDate, String remark) {
        log.info("[createTasksForAllShops] 开始为所有店铺创建广告同步任务，beginDate={}, endDate={}, remark={}", 
                beginDate, endDate, remark);
        
        // 获取店铺列表
        List<OzonShopMappingDO> shopList = ozonShopMappingService.getOzonShopList();
        if (CollectionUtils.isEmpty(shopList)) {
            log.warn("[createTasksForAllShops] 暂无店铺");
            return new TaskCreationResult(0, 0, 0, "暂无店铺");
        }
        
        // 过滤Ozon平台的店铺
        List<OzonShopMappingDO> ozonShopList = shopList.stream()
                .filter(shop -> shop.getPlatform().equals(DmPlatformEnum.OZON.getPlatformId())
                        || shop.getPlatform().equals(DmPlatformEnum.OZON_GLOBAL.getPlatformId()))
                .collect(Collectors.toList());
        
        int createdTaskCount = 0;
        int skippedCount = 0;
        
        // 为每个店铺创建同步任务
        for (OzonShopMappingDO shopMappingDO : ozonShopList) {
            try {
                int taskCount = createTasksForShop(shopMappingDO, beginDate, endDate, remark);
                createdTaskCount += taskCount;
            } catch (Exception e) {
                skippedCount++;
                log.error("[createTasksForAllShops] 为店铺创建同步任务失败，tenantId={}, clientId={}, error={}",
                        shopMappingDO.getTenantId(), shopMappingDO.getClientId(), e.getMessage(), e);
            }
        }
        
        String result = String.format("任务创建完成，共处理 %d 个店铺，成功创建 %d 个任务，跳过 %d 个",
                ozonShopList.size(), createdTaskCount, skippedCount);
        
        log.info("[createTasksForAllShops] {}", result);
        return new TaskCreationResult(ozonShopList.size(), createdTaskCount, skippedCount, result);
    }

    /**
     * 为单个店铺创建广告同步任务
     *
     * @param shopMappingDO 店铺映射信息
     * @param beginDate     开始日期
     * @param endDate       结束日期
     * @param remark        备注
     * @return 创建的任务数量
     */
    public int createTasksForShop(OzonShopMappingDO shopMappingDO, LocalDate beginDate, LocalDate endDate, String remark) {
        if (StringUtils.isBlank(shopMappingDO.getAdClientId()) || StringUtils.isBlank(shopMappingDO.getAdClientSecret())) {
            log.warn("[createTasksForShop] 广告账户没有配置，shopId={}", shopMappingDO.getClientId());
            throw new RuntimeException("广告账户没有配置");
        }
        
        // 获取该店铺的广告活动列表
        String beginDateStr = beginDate.format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
        String endDateStr = endDate.format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
        List<String> campaignIds = getCampaignIds(shopMappingDO.getClientId(), beginDateStr, endDateStr);
        
        if (CollectionUtils.isEmpty(campaignIds)) {
            log.warn("[createTasksForShop] 未获取到广告活动列表，clientId={}", shopMappingDO.getClientId());
            return 0;
        }

        // 批量创建同步任务（按广告活动ID分批，每批最多5个）
        List<Long> taskIds = ozonAdSyncTaskService.createBatchSyncTasks(
                shopMappingDO.getTenantId(),
                shopMappingDO.getClientId(),
                beginDate,
                endDate,
                campaignIds,
                remark
        );
        
        log.info("[createTasksForShop] 成功创建同步任务，tenantId={}, clientId={}, beginDate={}, endDate={}, 创建任务数={}, taskIds={}",
                shopMappingDO.getTenantId(), shopMappingDO.getClientId(), beginDate, endDate, taskIds.size(), taskIds);
        
        return taskIds.size();
    }

    /**
     * 获取广告活动ID列表
     *
     * @param clientId  客户端ID
     * @param beginDate 开始日期字符串
     * @param endDate   结束日期字符串
     * @return 广告活动ID列表
     */
    public List<String> getCampaignIds(String clientId, String beginDate, String endDate) {
        OzonShopMappingDO shopMappingDO = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        if (null == shopMappingDO) {
            log.error("没有找到对应ozon店铺");
            throw exception(OZON_SHOP_MAPPING_NOT_EXISTS);
        }

        AdDailyDTO adDaily = getAdDaily(shopMappingDO.getClientId(), beginDate, endDate);
        if (Objects.isNull(adDaily) || CollectionUtils.isEmpty(adDaily.getRows())) {
            log.warn("广告账户没有数据");
            return null;
        }

        List<AdDailyItemDTO> adDailyRows = adDaily.getRows();
        
        // 同步广告活动数据到数据库
        syncAdCampaignsData(shopMappingDO, adDailyRows);
        
        // 返回广告活动ID列表
        return adDailyRows.stream()
                .map(AdDailyItemDTO::getCampaignId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 同步广告活动数据到数据库
     *
     * @param shopMappingDO 店铺映射信息
     * @param adDailyRows   广告日报数据
     */
    private void syncAdCampaignsData(OzonShopMappingDO shopMappingDO, List<AdDailyItemDTO> adDailyRows) {
        for (AdDailyItemDTO adDailyRow : adDailyRows) {
            OzonAdCampaignsDO adCampaignsDO = new OzonAdCampaignsDO();
            adCampaignsDO.setClientId(shopMappingDO.getClientId());
            adCampaignsDO.setCampaignId(adDailyRow.getCampaignId());
            adCampaignsDO.setTitle(adDailyRow.getTitle());
            adCampaignsDO.setViews(Integer.valueOf(adDailyRow.getViews()));
            adCampaignsDO.setClicks(Integer.valueOf(adDailyRow.getClicks()));
            adCampaignsDO.setMoneySpent(new BigDecimal(adDailyRow.getMoneySpent()));
            adCampaignsDO.setAvgBid(new BigDecimal(adDailyRow.getAvgBid()));
            adCampaignsDO.setOrders(Integer.valueOf(adDailyRow.getOrders()));
            adCampaignsDO.setOrdersMoney(new BigDecimal(adDailyRow.getOrdersMoney()));
            adCampaignsDO.setTenantId(shopMappingDO.getTenantId());
            LocalDate localDate = LocalDate.parse(adDailyRow.getDate(), DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
            adCampaignsDO.setDate(localDate);

            OzonAdCampaignsDO existAdCampaign = ozonAdCampaignsService.getOzonAdCampaignByCampaignId(adDailyRow.getCampaignId(), localDate);
            if (null == existAdCampaign) {
                ozonAdCampaignsService.createOzonAdCampaigns(BeanUtils.toBean(adCampaignsDO, OzonAdCampaignsSaveReqVO.class));
            } else {
                adCampaignsDO.setId(existAdCampaign.getId());
                ozonAdCampaignsService.updateOzonAdCampaigns(BeanUtils.toBean(adCampaignsDO, OzonAdCampaignsSaveReqVO.class));
            }
        }
    }

    /**
     * 获取广告日报数据
     *
     * @param clientId  客户端ID
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return 广告日报数据
     */
    private AdDailyDTO getAdDaily(String clientId, String beginDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", beginDate);
        params.put("dateTo", endDate);

        TypeReference<AdDailyDTO> typeReference = new TypeReference<AdDailyDTO>() {
        };

        return ozonAdHttpUtil.get(clientId, OzonConfig.OZON_AD_DAILY, params, typeReference);
    }

    /**
     * 任务创建结果
     */
    public static class TaskCreationResult {
        private final int totalShops;
        private final int createdTasks;
        private final int skippedShops;
        private final String message;

        public TaskCreationResult(int totalShops, int createdTasks, int skippedShops, String message) {
            this.totalShops = totalShops;
            this.createdTasks = createdTasks;
            this.skippedShops = skippedShops;
            this.message = message;
        }

        public int getTotalShops() {
            return totalShops;
        }

        public int getCreatedTasks() {
            return createdTasks;
        }

        public int getSkippedShops() {
            return skippedShops;
        }

        public String getMessage() {
            return message;
        }
    }
}