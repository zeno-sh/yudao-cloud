package cn.iocoder.yudao.module.dm.controller.admin.shop;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.OZON_SHOP_MAPPING_EXISTS;

import cn.iocoder.yudao.module.dm.controller.admin.shop.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;

@Tag(name = "管理后台 - ozon店铺")
@RestController
@RequestMapping("/dm/ozon-shop-mapping")
@Validated
public class OzonShopMappingController {

    @Resource
    private OzonShopMappingService ozonShopMappingService;

    @PostMapping("/create")
    @Operation(summary = "创建ozon店铺")
    @PreAuthorize("@ss.hasPermission('dm:ozon-shop-mapping:create')")
    public CommonResult<Integer> createOzonShopMapping(@Valid @RequestBody OzonShopMappingSaveReqVO createReqVO) {
        validateClientId(createReqVO.getClientId());
        return success(ozonShopMappingService.createOzonShopMapping(createReqVO));
    }

    private void validateClientId(String clientId) {
        OzonShopMappingDO shop = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        if (shop != null) {
            throw exception(OZON_SHOP_MAPPING_EXISTS);
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新ozon店铺")
    @PreAuthorize("@ss.hasPermission('dm:ozon-shop-mapping:update')")
    public CommonResult<Boolean> updateOzonShopMapping(@Valid @RequestBody OzonShopMappingSaveReqVO updateReqVO) {
        ozonShopMappingService.updateOzonShopMapping(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ozon店铺")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:ozon-shop-mapping:delete')")
    public CommonResult<Boolean> deleteOzonShopMapping(@RequestParam("id") Integer id) {
        ozonShopMappingService.deleteOzonShopMapping(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ozon店铺")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:ozon-shop-mapping:query')")
    public CommonResult<OzonShopMappingRespVO> getOzonShopMapping(@RequestParam("id") Integer id) {
        OzonShopMappingDO ozonShopMapping = ozonShopMappingService.getOzonShopMapping(id);
        return success(BeanUtils.toBean(ozonShopMapping, OzonShopMappingRespVO.class));
    }

    @GetMapping("/get-simple-list")
    @Operation(summary = "获得ozon店铺")
    @PreAuthorize("@ss.hasPermission('dm:ozon-shop-mapping:query')")
    public CommonResult<List<OzonShopMappingSimpleRespVO>> getOzonShopMappingByAuth() {
        return success(ozonShopMappingService.getOzonShopMappingByAuth());
    }

    @GetMapping("/page")
    @Operation(summary = "获得ozon店铺分页")
    @PreAuthorize("@ss.hasPermission('dm:ozon-shop-mapping:query')")
    public CommonResult<PageResult<OzonShopMappingRespVO>> getOzonShopMappingPage(
            @Valid OzonShopMappingPageReqVO pageReqVO) {
        PageResult<OzonShopMappingDO> pageResult = ozonShopMappingService.getOzonShopMappingPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, OzonShopMappingRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ozon店铺 Excel")
    @PreAuthorize("@ss.hasPermission('dm:ozon-shop-mapping:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOzonShopMappingExcel(@Valid OzonShopMappingPageReqVO pageReqVO,
            HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OzonShopMappingDO> list = ozonShopMappingService.getOzonShopMappingPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "ozon店铺.xls", "数据", OzonShopMappingRespVO.class,
                BeanUtils.toBean(list, OzonShopMappingRespVO.class));
    }

    @GetMapping({ "/list-all-simple", "/simple-list" })
    @Operation(summary = "获取授权门店列表", description = "只包含被门店ID，门店名称，主要用于前端的下拉选项")
    public CommonResult<List<OzonShopRespVO>> getSimpleRoleList() {
        List<OzonShopMappingDO> ozonShopList = ozonShopMappingService.getOzonShopList();
        return success(BeanUtils.toBean(ozonShopList, OzonShopRespVO.class));
    }

    @PostMapping("/sync")
    @Operation(summary = "同步ozon店铺")
    @PreAuthorize("@ss.hasPermission('dm:ozon-shop-mapping:sync')")
    public CommonResult<Boolean> syncOzonShopMapping() {
        ozonShopMappingService.syncOzonShop();
        return success(true);
    }

}