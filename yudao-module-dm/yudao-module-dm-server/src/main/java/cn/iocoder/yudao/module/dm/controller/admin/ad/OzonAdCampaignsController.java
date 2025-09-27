package cn.iocoder.yudao.module.dm.controller.admin.ad;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdCampaignsPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdCampaignsRespVO;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdCampaignsSaveReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.OzonAdCampaignsSyncReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.AuthShopMappingService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.OzonAdTaskCreationService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.ReportAdService;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdAsyncProcessor;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdCampaignsService;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdSyncTaskService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - 广告活动")
@RestController
@RequestMapping("/dm/ozon-ad-campaigns")
@Validated
@Slf4j
public class OzonAdCampaignsController {

    @Resource
    private OzonAdCampaignsService ozonAdCampaignsService;
    @Resource
    private ReportAdService reportAdService;
    @Resource
    private AuthShopMappingService authShopMappingService;
    @Resource
    private OzonShopMappingService shopMappingService;
    @Resource
    private OzonAdSyncTaskService ozonAdSyncTaskService;
    @Resource
    private OzonAdAsyncProcessor ozonAdAsyncProcessor;
    @Resource
    private OzonAdTaskCreationService ozonAdTaskCreationService;

    @PostMapping("/create")
    @Operation(summary = "创建广告活动")
    @PreAuthorize("@ss.hasPermission('dm:ozon-ad-campaigns:create')")
    public CommonResult<Integer> createOzonAdCampaigns(@Valid @RequestBody OzonAdCampaignsSaveReqVO createReqVO) {
        return success(ozonAdCampaignsService.createOzonAdCampaigns(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新广告活动")
    @PreAuthorize("@ss.hasPermission('dm:ozon-ad-campaigns:update')")
    public CommonResult<Boolean> updateOzonAdCampaigns(@Valid @RequestBody OzonAdCampaignsSaveReqVO updateReqVO) {
        ozonAdCampaignsService.updateOzonAdCampaigns(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除广告活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:ozon-ad-campaigns:delete')")
    public CommonResult<Boolean> deleteOzonAdCampaigns(@RequestParam("id") Integer id) {
        ozonAdCampaignsService.deleteOzonAdCampaigns(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得广告活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:ozon-ad-campaigns:query')")
    public CommonResult<OzonAdCampaignsRespVO> getOzonAdCampaigns(@RequestParam("id") Integer id) {
        OzonAdCampaignsDO ozonAdCampaigns = ozonAdCampaignsService.getOzonAdCampaigns(id);
        return success(BeanUtils.toBean(ozonAdCampaigns, OzonAdCampaignsRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得广告活动分页")
    @PreAuthorize("@ss.hasPermission('dm:ozon-ad-campaigns:query')")
    public CommonResult<PageResult<OzonAdCampaignsRespVO>> getOzonAdCampaignsPage(@Valid OzonAdCampaignsPageReqVO pageReqVO) {

        String[] clientIds = getClientIds(pageReqVO.getClientIds());
        pageReqVO.setClientIds(clientIds);

        PageResult<OzonAdCampaignsDO> pageResult = ozonAdCampaignsService.getOzonAdCampaignsPage(pageReqVO);

        List<OzonShopMappingDO> ozonShopMappingDOList = shopMappingService.batchShopListByClientIds(Arrays.asList(clientIds));
        Map<String, OzonShopMappingDO> shopMappingDOMap = convertMap(ozonShopMappingDOList, OzonShopMappingDO::getClientId);

        return success(BeanUtils.toBean(pageResult, OzonAdCampaignsRespVO.class, vo -> {
            MapUtils.findAndThen(shopMappingDOMap, vo.getClientId(), ozonShopMappingDO -> vo.setShopName(ozonShopMappingDO.getShopName()));
            MapUtils.findAndThen(shopMappingDOMap, vo.getClientId(), ozonShopMappingDO -> vo.setPlatform(ozonShopMappingDO.getPlatform()));
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出广告活动 Excel")
    @PreAuthorize("@ss.hasPermission('dm:ozon-ad-campaigns:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOzonAdCampaignsExcel(@Valid OzonAdCampaignsPageReqVO pageReqVO,
                                           HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OzonAdCampaignsDO> list = ozonAdCampaignsService.getOzonAdCampaignsPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "广告活动.xls", "数据", OzonAdCampaignsRespVO.class,
                BeanUtils.toBean(list, OzonAdCampaignsRespVO.class));
    }

    @PostMapping("/sync")
    @Operation(summary = "创建广告活动")
    @PreAuthorize("@ss.hasPermission('dm:ozon-ad-campaigns:create')")
    public CommonResult<String> syncOzonAdCampaigns(@Valid @RequestBody OzonAdCampaignsSyncReqVO syncReqVO) {
        String begin = syncReqVO.getDate()[0];
        String end = syncReqVO.getDate()[1];

        long limitDay = DateUtil.betweenDay(DateUtil.parseDate(begin), DateUtil.parseDate(end), true);
        if (limitDay > 30) {
            throw ServiceExceptionUtil.invalidParamException("同步时间不能超过30天");
        }

        LocalDate beginDate = LocalDateTimeUtil.parseDate(begin, DatePattern.NORM_DATE_PATTERN);
        LocalDate endDate = LocalDateTimeUtil.parseDate(end, DatePattern.NORM_DATE_PATTERN);
        
        String[] clientIds = getClientIds(syncReqVO.getClientIds());
        int totalCreatedTasks = 0;
        
        for (String clientId : clientIds) {
            try {
                // 获取店铺信息
                OzonShopMappingDO shopMapping = shopMappingService.getOzonShopMappingByClientId(clientId);
                if (shopMapping == null) {
                    continue;
                }
                
                // 使用新的任务创建服务为单个店铺创建任务
                int createdTasks = ozonAdTaskCreationService.createTasksForShop(
                        shopMapping, beginDate, endDate, "手动触发");
                totalCreatedTasks += createdTasks;
                
            } catch (Exception e) {
                log.error("[syncOzonAdCampaigns][clientId({}) 处理异常，请进行处理！]", clientId, e);
            }
        }
        
        return success(String.format("同步成功，共创建 %d 个任务", totalCreatedTasks));
    }

    // ==================== 子表（广告明细） ====================

    @GetMapping("/ozon-ad-campaigns-item/list-by-campaign-id")
    @Operation(summary = "获得广告明细列表")
    @Parameter(name = "campaignId", description = "活动ID")
    @PreAuthorize("@ss.hasPermission('dm:ozon-ad-campaigns:query')")
    public CommonResult<List<OzonAdCampaignsItemDO>> getOzonAdCampaignsItemListByCampaignId(@RequestParam("campaignId") String campaignId,
                                                                                            @RequestParam("date") String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
        return success(ozonAdCampaignsService.getOzonAdCampaignsItemListByCampaignId(campaignId, localDate));
    }

    private String[] getClientIds(String[] clientIds) {
        if (null == clientIds || clientIds.length == 0) {
            List<OzonShopMappingDO> authShopList = authShopMappingService.getAuthShopList();
            return convertList(authShopList, OzonShopMappingDO::getClientId).toArray(new String[0]);
        }
        return clientIds;
    }

}