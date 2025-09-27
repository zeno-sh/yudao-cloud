package cn.iocoder.yudao.module.dm.controller.admin.online;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.online.vo.OzonProductOnlinePageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.online.vo.OzonProductOnlineRespVO;
import cn.iocoder.yudao.module.dm.controller.admin.online.vo.OzonProductOnlineSaveReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.online.vo.OzonProductOnlineSyncReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.AuthShopMappingService;
import cn.iocoder.yudao.module.dm.infrastructure.ProductOnlineManagerService;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - 在线商品")
@RestController
@RequestMapping("/dm/ozon-product-online")
@Validated
public class OzonProductOnlineController {

    @Resource
    private OzonProductOnlineService ozonProductOnlineService;
    @Resource
    private ProductOnlineManagerService productOnlineManagerService;
    @Resource
    private AuthShopMappingService authShopMappingService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;

    @PostMapping("/create")
    @Operation(summary = "创建在线商品")
    @PreAuthorize("@ss.hasPermission('dm:ozon-product-online:create')")
    public CommonResult<Long> createOzonProductOnline(@Valid @RequestBody OzonProductOnlineSaveReqVO createReqVO) {
        return success(ozonProductOnlineService.createOzonProductOnline(createReqVO));
    }

    @PostMapping("/sync")
    @Operation(summary = "同步在线商品")
    @PreAuthorize("@ss.hasPermission('dm:ozon-product-online:create')")
    public CommonResult<String> syncOzonProductOnline(@Valid @RequestBody OzonProductOnlineSyncReqVO syncReqVO) {

        String[] clientIds = getClientIds(syncReqVO.getClientIds());
        for (String clientId : clientIds) {
            CompletableFuture.runAsync(() -> productOnlineManagerService.syncOnlineProduct(clientId));

        }
        return success("同步成功,请稍后查看");
    }

    @PutMapping("/update")
    @Operation(summary = "更新在线商品")
    @PreAuthorize("@ss.hasPermission('dm:ozon-product-online:update')")
    public CommonResult<Boolean> updateOzonProductOnline(@Valid @RequestBody OzonProductOnlineSaveReqVO updateReqVO) {
        ozonProductOnlineService.updateOzonProductOnline(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除在线商品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:ozon-product-online:delete')")
    public CommonResult<Boolean> deleteOzonProductOnline(@RequestParam("id") Long id) {
        ozonProductOnlineService.deleteOzonProductOnline(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得在线商品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:ozon-product-online:query')")
    public CommonResult<OzonProductOnlineRespVO> getOzonProductOnline(@RequestParam("id") Long id) {
        OzonProductOnlineDO ozonProductOnline = ozonProductOnlineService.getOzonProductOnline(id);
        return success(BeanUtils.toBean(ozonProductOnline, OzonProductOnlineRespVO.class));
    }

    @GetMapping("/get-url")
    @Operation(summary = "获得在线商品")
    @PreAuthorize("@ss.hasPermission('dm:ozon-product-online:query')")
    public CommonResult<String> getPlatformUrl(@RequestParam("clientId") String clientId, @RequestParam("offerId") String offerId) {

        return success(ozonProductOnlineService.getPlatformUrl(clientId, offerId));
    }

    @GetMapping("/get-offer-id")
    @Operation(summary = "获得在线商品")
    @Parameter(name = "offerId", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:ozon-product-online:query')")
    public CommonResult<List<OzonProductOnlineRespVO>> getOzonProductOnlineByOfferId(@RequestParam("offerId") String offerId) {
        List<OzonProductOnlineDO> ozonProductOnline = ozonProductOnlineService.getProductOnlineByOfferId(offerId);
        return success(BeanUtils.toBean(ozonProductOnline, OzonProductOnlineRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得在线商品分页")
    @PreAuthorize("@ss.hasPermission('dm:ozon-product-online:query')")
    public CommonResult<PageResult<OzonProductOnlineRespVO>> getOzonProductOnlinePage(@Valid OzonProductOnlinePageReqVO pageReqVO) {
        String[] clientIds = getClientIds(pageReqVO.getClientIds());
        pageReqVO.setClientIds(clientIds);

        List<OzonShopMappingDO> ozonShopMappingDOList = ozonShopMappingService.batchShopListByClientIds(Arrays.asList(clientIds));
        Map<String, OzonShopMappingDO> shopMappingDOMap = convertMap(ozonShopMappingDOList, OzonShopMappingDO::getClientId);

        PageResult<OzonProductOnlineDO> pageResult = ozonProductOnlineService.getOzonProductOnlinePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, OzonProductOnlineRespVO.class, vo->{
            MapUtils.findAndThen(shopMappingDOMap, vo.getClientId(), ozonShopMappingDO -> vo.setShopName(ozonShopMappingDO.getShopName()));
            MapUtils.findAndThen(shopMappingDOMap, vo.getClientId(), ozonShopMappingDO -> vo.setPlatform(ozonShopMappingDO.getPlatform()));
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出在线商品 Excel")
    @PreAuthorize("@ss.hasPermission('dm:ozon-product-online:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOzonProductOnlineExcel(@Valid OzonProductOnlinePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OzonProductOnlineDO> list = ozonProductOnlineService.getOzonProductOnlinePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "在线商品.xls", "数据", OzonProductOnlineRespVO.class,
                        BeanUtils.toBean(list, OzonProductOnlineRespVO.class));
    }

    private String[] getClientIds(String[] clientIds) {
        if (null == clientIds || clientIds.length == 0) {
            List<OzonShopMappingDO> authShopList = authShopMappingService.getAuthShopList();
            return convertList(authShopList, OzonShopMappingDO::getClientId).toArray(new String[0]);
        }
        return clientIds;
    }

}