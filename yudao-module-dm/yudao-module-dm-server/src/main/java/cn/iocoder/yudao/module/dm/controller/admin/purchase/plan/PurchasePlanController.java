package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;
import cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.dm.enums.PurchasePlanStatusEnum;
import cn.iocoder.yudao.module.dm.rpc.PurchasePlanLifeService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.purchase.PurchasePlanService;
import cn.iocoder.yudao.module.dm.service.purchase.log.PurchaseOrderArrivedLogService;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderService;
import cn.iocoder.yudao.module.dm.service.purchase.plan.PurchasePlanItemService;
import cn.iocoder.yudao.module.dm.service.transport.TransportPlanService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;

@Tag(name = "管理后台 - 采购计划")
@RestController
@RequestMapping("/dm/purchase-plan")
@Validated
public class PurchasePlanController {

    @Resource
    private PurchasePlanService purchasePlanService;
    @Resource
    private ProductInfoService productInfoService;
    @Resource
    private PurchasePlanItemService purchasePlanItemService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PurchaseOrderArrivedLogService purchaseOrderArrivedLogService;
    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private TransportPlanService transportPlanService;
    @Resource
    private PurchasePlanLifeService purchasePlanLifeService;


    @PostMapping("/create")
    @Operation(summary = "创建采购计划")
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:create')")
    public CommonResult<Long> createPurchasePlan(@Valid @RequestBody PurchasePlanSaveReqVO createReqVO) {
        return success(purchasePlanService.createPurchasePlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购计划")
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:update')")
    public CommonResult<Boolean> updatePurchasePlan(@Valid @RequestBody PurchasePlanSaveReqVO updateReqVO) {
        purchasePlanService.updatePurchasePlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:delete')")
    public CommonResult<Boolean> deletePurchasePlan(@RequestParam("id") Long id) {
        purchasePlanService.deletePurchasePlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购计划")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:query')")
    public CommonResult<PurchasePlanRespVO> getPurchasePlan(@RequestParam("id") Long id) {
        PurchasePlanDO purchasePlan = purchasePlanService.getPurchasePlan(id);
        PurchasePlanRespVO planRespVO = BeanUtils.toBean(purchasePlan, PurchasePlanRespVO.class);
        if (purchasePlan == null) {
            return success(null);
        }

        List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanService.getPurchasePlanItemListByPlanId(id);
        if (CollectionUtils.isNotEmpty(purchasePlanItemList)) {
            planRespVO.setPurchasePlanItems(buildPurchasePlanItemRespVOList(purchasePlanItemList));
            planRespVO.setTotalQuantity(purchasePlanItemList.stream().mapToInt(PurchasePlanItemDO::getQuantity).sum());
        }

        String creatorId = purchasePlan.getCreator();
        AdminUserRespDTO user = adminUserApi.getUser(Long.valueOf(creatorId)).getCheckedData();
        planRespVO.setCreator(user.getNickname());

        return success(planRespVO);
    }

    @GetMapping("/pageOld")
    @Operation(summary = "获得采购计划分页")
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:query')")
    public CommonResult<PageResult<PurchasePlanRespVO>> getPurchasePlanPageOld(@Valid PurchasePlanPageReqVO pageReqVO) {
        PageResult<PurchasePlanDO> pageResult = purchasePlanService.getPurchasePlanPage(pageReqVO);
        List<PurchasePlanDO> purchasePlanDOList = pageResult.getList();

        List<PurchasePlanRespVO> purchasePlanRespVOList = new ArrayList<>();
        for (PurchasePlanDO purchasePlanDO : purchasePlanDOList) {
            PurchasePlanRespVO planRespVO = BeanUtils.toBean(purchasePlanDO, PurchasePlanRespVO.class);
            String creatorId = purchasePlanDO.getCreator();
            AdminUserRespDTO user = adminUserApi.getUser(Long.valueOf(creatorId)).getCheckedData();
            planRespVO.setCreator(user.getNickname());
            purchasePlanRespVOList.add(planRespVO);
        }

        PageResult<PurchasePlanRespVO> newResult = new PageResult<>();
        newResult.setTotal(pageResult.getTotal());
        newResult.setList(purchasePlanRespVOList);

        return success(newResult);
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购计划分页")
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:query')")
    public CommonResult<PageResult<PurchasePlanRespVO>> getPurchasePlanPage(@Valid PurchasePlanPageReqVO pageReqVO) {
        PageResult<PurchasePlanDO> pageResult = purchasePlanService.getPurchasePlanPage(pageReqVO);

        List<PurchasePlanDO> purchasePlanDOList = pageResult.getList();

        // 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(purchasePlanDOList,
                purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator()))));

        List<PurchasePlanRespVO> purchasePlanRespVOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(purchasePlanDOList)) {
            for (PurchasePlanDO purchasePlanDO : purchasePlanDOList) {
                PurchasePlanRespVO planRespVO = BeanUtils.toBean(purchasePlanDO, PurchasePlanRespVO.class);
                Long planId = purchasePlanDO.getId();
                List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanService.getPurchasePlanItemListByPlanId(planId);
                if (CollectionUtils.isNotEmpty(purchasePlanItemList)) {
                    int totalQuantity = purchasePlanItemList.stream().mapToInt(PurchasePlanItemDO::getQuantity).sum();
                    planRespVO.setTotalQuantity(totalQuantity);
                }
                planRespVO.setPurchasePlanItems(buildPurchasePlanItemRespVOList(purchasePlanItemList));
                String creatorId = purchasePlanDO.getCreator();
                AdminUserRespDTO user = userMap.get(Long.valueOf(creatorId));
                if (user != null) {
                    planRespVO.setCreatorName(user.getNickname());
                }
                purchasePlanRespVOList.add(planRespVO);
            }
        }

        PageResult<PurchasePlanRespVO> newResult = new PageResult<>();
        newResult.setTotal(pageResult.getTotal());
        newResult.setList(purchasePlanRespVOList);

        return success(newResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购计划 Excel")
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchasePlanExcel(@Valid PurchasePlanPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PurchasePlanDO> list = purchasePlanService.getPurchasePlanPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "采购计划.xls", "数据", PurchasePlanRespVO.class, BeanUtils.toBean(list, PurchasePlanRespVO.class));
    }

    // ==================== 子表（采购计划详情） ====================

    @GetMapping("/purchase-plan-item/list-by-plan-id-old")
    @Operation(summary = "获得采购计划详情列表")
    @Parameter(name = "planId", description = "计划id")
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:query')")
    public CommonResult<List<PurchasePlanItemDO>> getPurchasePlanItemListByPlanIdOld(@RequestParam("planId") Long planId) {
        return success(purchasePlanService.getPurchasePlanItemListByPlanId(planId));
    }

    @GetMapping("/purchase-plan-item/list-by-plan-id")
    @Operation(summary = "获得采购计划详情列表")
    @Parameter(name = "planId", description = "计划id")
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:query')")
    public CommonResult<List<PurchasePlanItemRespVO>> getPurchasePlanItemListByPlanId(@RequestParam("planId") Long planId) {
        List<PurchasePlanItemDO> purchasePlanItemDOS = purchasePlanService.getPurchasePlanItemListByPlanId(planId);
        return success(buildPurchasePlanItemRespVOList(purchasePlanItemDOS));
    }

    @GetMapping("/purchase-plan-item/list-by-plan-number")
    @Operation(summary = "获得采购计划详情列表")
    @Parameter(name = "planId", description = "计划id")
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:query')")
    public CommonResult<PurchasePlanItemRespVO> getPurchasePlanItemListByPlanNumber(@RequestParam("planNumber") String planNumber) {
        PurchasePlanItemDO purchasePlanItem = purchasePlanItemService.getPurchasePlanItemByPlanNumber(planNumber);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(Lists.newArrayList(NumberUtils.parseLong(purchasePlanItem.getCreator())));
        return success(BeanUtils.toBean(purchasePlanItem, PurchasePlanItemRespVO.class, purchasePlanItemRespVO -> {
                    MapUtils.findAndThen(userMap, NumberUtils.parseLong(purchasePlanItem.getCreator()), user -> purchasePlanItemRespVO.setCreatorName(user.getNickname()));
                }
        ));
    }

    @Idempotent(timeout = 10, timeUnit = TimeUnit.SECONDS, message = "正在提交审批，请勿重复提交")
    @PutMapping("/review")
    @Operation(summary = "提交审批采购计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:create')")
    public CommonResult<Boolean> submitReviewPurchasePlan(@RequestParam("id") Long id) {
        purchasePlanService.submitReview(id);
        return success(true);
    }

    @GetMapping("/item-page")
    @Operation(summary = "获得采购计划详情分页")
//    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:query')")
    @DataPermission(enable = false)
    public CommonResult<PageResult<PurchasePlanItemRespVO>> getPurchasePlanItemPage(@Valid PurchasePlanItemPageReqVO pageReqVO) {
        PageResult<PurchasePlanItemDO> pageResult = purchasePlanItemService.getPurchasePlanItemPage(pageReqVO);

        PageResult<PurchasePlanItemRespVO> newResult = new PageResult<>();
        newResult.setList(buildPurchasePlanItemRespVOList(pageResult.getList()));
        newResult.setTotal(pageResult.getTotal());
        return success(newResult);
    }

    @DeleteMapping("/delete-item")
    @Operation(summary = "作废采购计划明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:purchase-plan:delete')")
    public CommonResult<Boolean> deletePurchasePlanItem(@RequestParam("planNumber") String planNumber) {
        purchasePlanService.updatePurchasePlanItemStatus(planNumber, PurchasePlanStatusEnum.DO_DELETED.getStatus());
        return success(true);
    }

    private List<PurchasePlanItemRespVO> buildPurchasePlanItemRespVOList(List<PurchasePlanItemDO> purchasePlanItemDOS) {
        List<PurchasePlanItemRespVO> purchasePlanItemRespVOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(purchasePlanItemDOS)) {
            List<Long> planIds = purchasePlanItemDOS.stream().map(PurchasePlanItemDO::getPlanId).collect(Collectors.toList());
            List<Long> productIds = purchasePlanItemDOS.stream().map(PurchasePlanItemDO::getProductId).collect(Collectors.toList());
            // 管理员信息
            Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(purchasePlanItemDOS, itemDO -> Stream.of(NumberUtils.parseLong(itemDO.getCreator()))));

            List<ProductInfoDO> productInfoDOList = productInfoService.batchQueryProductInfoList(productIds);
            List<PurchasePlanDO> purchasePlanDOList = purchasePlanService.batchQueryPurchasePlan(planIds);

            Map<Long, PurchasePlanDO> purchasePlanDOMap = purchasePlanDOList.stream().collect(Collectors.toMap(PurchasePlanDO::getId, Function.identity(), (v1, v2) -> v1));
            Map<Long, ProductInfoDO> productInfoDOMap = productInfoDOList.stream().collect(Collectors.toMap(ProductInfoDO::getId, Function.identity(), (v1, v2) -> v1));

            for (PurchasePlanItemDO purchasePlanItemDO : purchasePlanItemDOS) {
                PurchasePlanItemRespVO vo = BeanUtils.toBean(purchasePlanItemDO, PurchasePlanItemRespVO.class);
                ProductInfoDO productInfoDO = productInfoDOMap.get(purchasePlanItemDO.getProductId());
                if (productInfoDO != null) {
                    ProductSimpleInfoVO productSimpleInfoVO = new ProductSimpleInfoVO();
                    productSimpleInfoVO.setProductId(productInfoDO.getId());
                    productSimpleInfoVO.setSkuId(productInfoDO.getSkuId());
                    productSimpleInfoVO.setSkuName(productInfoDO.getSkuName());
                    productSimpleInfoVO.setImage(productInfoDO.getPictureUrl());
                    vo.setProductSimpleInfo(productSimpleInfoVO);
                }

                PurchasePlanDO purchasePlanDO = purchasePlanDOMap.get(purchasePlanItemDO.getPlanId());
                if (purchasePlanDO != null) {
                    vo.setBatchNumber(purchasePlanDO.getBatchNumber());
                }
                MapUtils.findAndThen(userMap, Long.parseLong(purchasePlanItemDO.getCreator()), user -> vo.setCreatorName(user.getNickname()));

                purchasePlanItemRespVOS.add(vo);

            }
        }
        return purchasePlanItemRespVOS;
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<PurchasePlanImportVO> list = Arrays.asList(
                PurchasePlanImportVO.builder()
                        .skuId("必填").skuName("非必填").quantity("必填").arrivedDate("非必填")
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "采购计划导入模板.xlsx", "采购列表", PurchasePlanImportVO.class, list);
    }


    @PostMapping("/import")
    @Operation(summary = "导入采购计划")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true)
    })
    @PreAuthorize("@ss.hasPermission('system:user:import')")
    public CommonResult<Long> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<PurchasePlanImportVO> list = ExcelUtils.read(file, PurchasePlanImportVO.class);
        return success(purchasePlanService.importPurchasePlan(list));
    }

    @PostMapping("/purchase-plan-item/life")
    @Operation(summary = "获得采购计划生命周期")
    public CommonResult<List<PurchasePlanLifeRespVO>> getPurchasePlanLife(@Valid @RequestBody PurchasePlanLifeReqVO purchasePlanLifeReqVO) {
        validateParam(purchasePlanLifeReqVO);
        return success(purchasePlanLifeService.queryPurchasePlanLife(purchasePlanLifeReqVO));
    }

    private void validateParam(PurchasePlanLifeReqVO purchasePlanLifeReqVO) {
        if (StringUtils.isBlank(purchasePlanLifeReqVO.getPlanNumber()) &&
                StringUtils.isBlank(purchasePlanLifeReqVO.getOrderNo()) && StringUtils.isBlank(purchasePlanLifeReqVO.getSku()) &&
                StringUtils.isBlank(purchasePlanLifeReqVO.getPlanBatchNumber())
                &&StringUtils.isBlank(purchasePlanLifeReqVO.getStatus()) && StringUtils.isBlank(purchasePlanLifeReqVO.getSpu())) {
            throw exception(ErrorCodeConstants.PURCHASE_PLAN_LIFE_EMPTY_ERROR);
        }
    }

}