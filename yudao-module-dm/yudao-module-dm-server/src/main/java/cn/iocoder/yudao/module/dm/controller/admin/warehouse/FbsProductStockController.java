package cn.iocoder.yudao.module.dm.controller.admin.warehouse;

import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import cn.iocoder.yudao.module.dm.infrastructure.fbs.WarehouseService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsWarehouseService;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.stream.Collectors;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsProductStockDO;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsProductStockService;

@Tag(name = "管理后台 - 海外仓产品库存")
@RestController
@RequestMapping("/dm/fbs-product-stock")
@Validated
public class FbsProductStockController {

    @Resource
    private FbsProductStockService fbsProductStockService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private FbsWarehouseService fbsWarehouseService;
    @Resource
    private ProductInfoService productInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建海外仓产品库存")
    @PreAuthorize("@ss.hasPermission('dm:fbs-product-stock:create')")
    public CommonResult<Long> createFbsProductStock(@Valid @RequestBody FbsProductStockSaveReqVO createReqVO) {
        return success(fbsProductStockService.createFbsProductStock(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新海外仓产品库存")
    @PreAuthorize("@ss.hasPermission('dm:fbs-product-stock:update')")
    public CommonResult<Boolean> updateFbsProductStock(@Valid @RequestBody FbsProductStockSaveReqVO updateReqVO) {
        fbsProductStockService.updateFbsProductStock(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除海外仓产品库存")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:fbs-product-stock:delete')")
    public CommonResult<Boolean> deleteFbsProductStock(@RequestParam("id") Long id) {
        fbsProductStockService.deleteFbsProductStock(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得海外仓产品库存")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:fbs-product-stock:query')")
    public CommonResult<FbsProductStockRespVO> getFbsProductStock(@RequestParam("id") Long id) {
        FbsProductStockDO fbsProductStock = fbsProductStockService.getFbsProductStock(id);
        Long productId = fbsProductStock.getProductId();
        Map<Long, ProductInfoDO> productInfoDOMap = new HashMap<>();
        if (Objects.nonNull(productId)) {
            ProductInfoDO productInfo = productInfoService.getProductInfo(productId);
            productInfoDOMap.put(productInfo.getId(), productInfo);
        }
        return success(BeanUtils.toBean(fbsProductStock, FbsProductStockRespVO.class, vo -> {
            MapUtils.findAndThen(productInfoDOMap, vo.getProductId(), productInfoDO -> {
                ProductSimpleInfoVO productSimpleInfoVO = new ProductSimpleInfoVO();
                productSimpleInfoVO.setImage(productInfoDO.getPictureUrl());
                productSimpleInfoVO.setProductId(productInfoDO.getId());
                productSimpleInfoVO.setSkuId(productInfoDO.getSkuId());
                productSimpleInfoVO.setSkuName(productInfoDO.getSkuName());
                vo.setProductSimpleInfo(productSimpleInfoVO);
            });
        }));
    }

    @GetMapping("/sync")
    @Operation(summary = "发起海外仓产品库存同步")
    @Parameter(name = "warehouseId", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:fbs-product-stock:query')")
    public CommonResult<String> syncStock(@RequestParam("warehouseId") Long warehouseId) {
        warehouseService.syncStock(warehouseId);
        return success("同步任务已提交,请稍后查看");
    }

    @GetMapping("/page")
    @Operation(summary = "获得海外仓产品库存分页")
    @PreAuthorize("@ss.hasPermission('dm:fbs-product-stock:query')")
    public CommonResult<PageResult<FbsProductStockRespVO>> getFbsProductStockPage(@Valid FbsProductStockPageReqVO pageReqVO) {
        PageResult<FbsProductStockDO> pageResult = fbsProductStockService.getFbsProductStockPage(pageReqVO);
        if (CollectionUtils.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        List<Long> warehouseIds = convertList(pageResult.getList(), FbsProductStockDO::getWarehouseId);
        List<FbsWarehouseDO> fbsWarehouseDOS = fbsWarehouseService.batchFbsWarehouse(warehouseIds);
        Map<Long, FbsWarehouseDO> fbsWarehouseDOMap = convertMap(fbsWarehouseDOS, FbsWarehouseDO::getId);

        Map<Long, ProductSimpleInfoVO> productInfoDOMap = new HashMap<>();
        List<Long> productIds = convertList(pageResult.getList(), FbsProductStockDO::getProductId)
                .stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(productIds)){
            productInfoDOMap = productInfoService.batchQueryProductSimpleInfo(productIds);
        }
        Map<Long, ProductSimpleInfoVO> finalProductInfoDOMap = productInfoDOMap;
        return success(BeanUtils.toBean(pageResult, FbsProductStockRespVO.class, vo -> {
            MapUtils.findAndThen(fbsWarehouseDOMap, vo.getWarehouseId(), fbsWarehouseDO -> vo.setWarehouseName(fbsWarehouseDO.getName()));
            MapUtils.findAndThen(finalProductInfoDOMap, vo.getProductId(), vo::setProductSimpleInfo);
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出海外仓产品库存 Excel")
    @PreAuthorize("@ss.hasPermission('dm:fbs-product-stock:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFbsProductStockExcel(@Valid FbsProductStockPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FbsProductStockDO> list = fbsProductStockService.getFbsProductStockPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "海外仓产品库存.xls", "数据", FbsProductStockRespVO.class,
                        BeanUtils.toBean(list, FbsProductStockRespVO.class));
    }

}