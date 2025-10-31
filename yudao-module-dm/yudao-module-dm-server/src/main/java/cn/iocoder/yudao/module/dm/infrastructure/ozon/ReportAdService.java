package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdCampaignsSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad.*;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonAdHttpUtil;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdCampaignsService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.github.rholder.retry.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.OZON_SHOP_MAPPING_NOT_EXISTS;

/**
 * 1.使用getAdExpense获取有费用支出的广告模板
 * 2.使用getAdDaily获取广告模板的每日花费，这个数据是活动维度
 * 3.使用getAdReportDetail获取广告模板的详细数据，这个数据是模板+sku维度
 *
 * @Author zeno
 * @Date 2024/2/5
 */
@Service
@Slf4j
public class ReportAdService {

    @Resource
    private OzonAdHttpUtil ozonAdHttpUtil;
    @Resource
    private ReportAdService reportAdService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private OzonAdCampaignsService ozonAdCampaignsService;

    private static final int MAX_RETRY = 15; // 最大重试次数
    private static final long WAIT_TIME = 60 * 1000; // 间隔时间，单位毫秒


    /**
     * 获取有费用支出的广告模板
     * 这里只能获取模板ID、日期、标题、费用支出，不能获取到其他信息
     *
     * @param clientId
     * @param beginDate
     * @param endDate
     * @return
     */
    public AdExpenseDTO getAdExpense(String clientId, String beginDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", DateUtil.format(DateUtil.parse(beginDate), DatePattern.NORM_DATE_PATTERN));
        params.put("dateTo", DateUtil.format(DateUtil.parse(endDate), DatePattern.NORM_DATE_PATTERN));

        TypeReference<AdExpenseDTO> typeReference = new TypeReference<AdExpenseDTO>() {
        };

        return ozonAdHttpUtil.get(clientId, OzonConfig.OZON_AD_EXPENSE_LIST, params, typeReference);
    }

    /**
     * 获取广告模板每日花费
     * 这里可以获取到活动维度的数据，点击、展示、订单量、广告花费等等
     *
     * @param clientId
     * @param beginDate
     * @param endDate
     * @return
     */
    public AdDailyDTO getAdDaily(String clientId, String beginDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", beginDate);
        params.put("dateTo", endDate);

        TypeReference<AdDailyDTO> typeReference = new TypeReference<AdDailyDTO>() {
        };

        return ozonAdHttpUtil.get(clientId, OzonConfig.OZON_AD_DAILY, params, typeReference);
    }

    /**
     * 根据模板Id获取广告详情
     * 这里可以获取Sku维度的数据，比如展示、点击、订单量、广告花费等等
     * 返回值是报告的UUID，需要通过getAdReportDetail获取详细数据
     *
     * @param clientId
     * @param campaignIds
     * @param beginDate   yyyy-MM-dd HH:mm:ss 格式
     * @param endDate
     * @return 报告UUID
     */
    public String getAdStatistics(String clientId, List<String> campaignIds, String beginDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("from", DmDateUtils.formatStartOfDay(beginDate, DatePattern.UTC_PATTERN));
        params.put("to", DmDateUtils.formatEndOfDay(endDate, DatePattern.UTC_PATTERN));
        params.put("groupBy", "DATE");
        if (CollectionUtils.isNotEmpty(campaignIds)) {
            params.put("campaigns", campaignIds);
        }

        TypeReference<String> typeReference = new TypeReference<String>() {
        };

        return ozonAdHttpUtil.post(clientId, OzonConfig.OZON_AD_STATISTICS, JSON.toJSONString(params), typeReference);
    }

    /**
     * 根据报告UUID获取详细数据
     * 先去查询报告的状态，只有状态ok，才能继续获取详情
     *
     * @param clientId
     * @param reportId
     * @return
     */
    public Map<String, AdReportCampaignDTO> getAdReportDetail(String clientId, String reportId) {
        if (!isReady(clientId, reportId)) {
            return null;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("UUID", reportId);

        TypeReference<Map<String, AdReportCampaignDTO>> typeReference = new TypeReference<Map<String, AdReportCampaignDTO>>() {
        };

        return ozonAdHttpUtil.get(clientId, OzonConfig.OZON_AD_REPORT_DETAIL, params, typeReference);
    }

    private Boolean isReady(String clientId, String reportId) {
        // 创建重试器
        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(result -> result != null && !result) // 如果结果不为null且为false，则进行重试
                .retryIfExceptionOfType(Exception.class) // 对所有异常类型进行重试
                .withWaitStrategy(WaitStrategies.fixedWait(WAIT_TIME, TimeUnit.MILLISECONDS)) // 固定等待时间
                .withStopStrategy(StopStrategies.stopAfterAttempt(MAX_RETRY)) // 最多重试MAX_RETRY次
                .build();

        try {
            // 执行重试操作
            return retryer.call(() -> {
                String response = ozonAdHttpUtil.getByPathVariables(clientId, OzonConfig.OZON_AD_REPORT_STATE, reportId);
                // 返回判断响应是否有效
                return isValidResponse(response);
            });
        } catch (RetryException e) {
            log.error("Error after max retry attempts", e);
            return false;
        } catch (ExecutionException e) {
            log.error("Execution error while checking if report is ready", e);
            return false;
        }
    }

    public void doSync(String clientId, String beginDate, String endDate) {

        OzonShopMappingDO shopMappingDO = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        if (null == shopMappingDO) {
            log.error("没有找到对应ozon店铺");
            throw exception(OZON_SHOP_MAPPING_NOT_EXISTS);
        }

        AdDailyDTO adDaily = getAdDaily(shopMappingDO.getClientId(), beginDate, endDate);
        if (Objects.isNull(adDaily) || CollectionUtils.isEmpty(adDaily.getRows())) {
            log.warn("广告账户没有数据");
        }

        List<AdDailyItemDTO> adDailyRows = adDaily.getRows();
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
        List<String> campaignIds = adDailyRows.stream().map(AdDailyItemDTO::getCampaignId).distinct().collect(Collectors.toList());

        List<List<String>> partition = Lists.partition(campaignIds, 5);
        for (List<String> part : partition) {
            syncAdCampaignsItem(shopMappingDO, part, beginDate, endDate);
        }
    }

    private void syncAdCampaignsItem(OzonShopMappingDO shopMappingDO, List<String> campaignIds, String beginDate, String endDate) {
        String adStatistics = reportAdService.getAdStatistics(shopMappingDO.getClientId(), campaignIds, beginDate, endDate);
        if (StringUtils.isBlank(adStatistics)) {
            return;
        }

        JSONObject jsonObject = JSONObject.parseObject(adStatistics);
        String uuid = (String) jsonObject.get("UUID");
        Map<String, AdReportCampaignDTO> adReportDetail = reportAdService.getAdReportDetail(shopMappingDO.getClientId(), uuid);


        List<OzonAdCampaignsItemDO> insertList = new ArrayList<>();
        List<OzonAdCampaignsItemDO> updateList = new ArrayList<>();
        adReportDetail.forEach((campaignId, adReportCampaignDTO) -> {
            List<AdReportSkuDTO> rows = adReportCampaignDTO.getReport().getRows();
            if (CollectionUtils.isNotEmpty(rows)) {
                for (AdReportSkuDTO row : rows) {
                    OzonAdCampaignsItemDO adCampaignsItemDO = new OzonAdCampaignsItemDO();

                    //搜索广告没有click这些值
                    if (StringUtils.isBlank(row.getOrderId())) {
                        adCampaignsItemDO.setViews(Objects.isNull(row.getViews()) ? 0 : Integer.valueOf(row.getViews()));
                        adCampaignsItemDO.setClicks(Objects.isNull(row.getClicks()) ? 0 : Integer.valueOf(row.getClicks()));
                        adCampaignsItemDO.setCr(Objects.isNull(row.getCr()) ? BigDecimal.ZERO : new BigDecimal(row.getCr()));
                        adCampaignsItemDO.setOrders(Objects.isNull(row.getOrders()) ? 0 : Integer.valueOf(row.getOrders()));
                        adCampaignsItemDO.setAvgBid(Objects.isNull(row.getAvgBid()) ? BigDecimal.ZERO : new BigDecimal(row.getAvgBid()));
                        adCampaignsItemDO.setOrdersMoney(new BigDecimal(row.getOrdersMoney()));
                    } else {
                        adCampaignsItemDO.setOrdersMoney(new BigDecimal(row.getCost()));
                        adCampaignsItemDO.setOrderId(row.getOrderId());
                        adCampaignsItemDO.setOrders(1);
                        adCampaignsItemDO.setAvgBid(new BigDecimal(row.getBidValue()));
                    }

                    adCampaignsItemDO.setClientId(shopMappingDO.getClientId());
                    adCampaignsItemDO.setPlatformSkuId(row.getSku());
                    adCampaignsItemDO.setMoneySpent(new BigDecimal(row.getMoneySpent()));
                    adCampaignsItemDO.setPrice(new BigDecimal(row.getPrice()));

                    LocalDate localDate = LocalDate.parse(row.getDate(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    adCampaignsItemDO.setDate(localDate);
                    adCampaignsItemDO.setCampaignId(campaignId);
                    adCampaignsItemDO.setTenantId(shopMappingDO.getTenantId());

                    // 搜索广告
                    OzonAdCampaignsItemDO existItemDO = null;
                    if (StringUtils.isNotBlank(row.getOrderId())) {
                        existItemDO = ozonAdCampaignsService.getOzonAdCampaignItemByOrderId(campaignId, row.getSku(), row.getOrderId(), localDate);
                    } else {
                        existItemDO = ozonAdCampaignsService.getOzonAdCampaignItemByCampaignId(campaignId, row.getSku(), localDate);
                    }

                    if (null == existItemDO) {
                        insertList.add(adCampaignsItemDO);
                    } else {
                        adCampaignsItemDO.setId(existItemDO.getId());
                        updateList.add(adCampaignsItemDO);
                    }
                }
            }
        });

        if (CollectionUtils.isNotEmpty(insertList)) {
            ozonAdCampaignsService.createOzonAdCampaignsItem(insertList);
        }

        if (CollectionUtils.isNotEmpty(updateList)) {
            ozonAdCampaignsService.updateOzonAdCampaignsItem(updateList);
        }
    }

    private boolean isValidResponse(String response) {
        if (StringUtils.isBlank(response)) {
            return false;
        }

        JSONObject jsonObject = JSONObject.parseObject(response);
        if (!jsonObject.get("state").equals("OK")) {
            return false;
        }
        return true;
    }
}
