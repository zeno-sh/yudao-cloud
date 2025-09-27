package cn.iocoder.yudao.module.dm.controller.admin.finance;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.service.finance.FinancePaymentService;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 付款单")
@RestController
@RequestMapping("/dm/finance-payment")
@Validated
public class FinancePaymentController {

    @Resource
    private FinancePaymentService financePaymentService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PurchaseOrderService purchaseOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建付款单")
    @PreAuthorize("@ss.hasPermission('dm:finance-payment:create')")
    public CommonResult<Long> createFinancePayment(@Valid @RequestBody FinancePaymentSaveReqVO createReqVO) {
        return success(financePaymentService.createFinancePayment(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新付款单")
    @PreAuthorize("@ss.hasPermission('dm:finance-payment:update')")
    public CommonResult<Boolean> updateFinancePayment(@Valid @RequestBody FinancePaymentSaveReqVO updateReqVO) {
        financePaymentService.updateFinancePayment(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-file")
    @Operation(summary = "更新付款凭证")
    @PreAuthorize("@ss.hasPermission('dm:finance-payment:update')")
    public CommonResult<Boolean> updateFinancePaymentFile(@Valid @RequestBody FinancePaymentSaveFileReqVO financePaymentSaveFileReqVO) {
        financePaymentService.updateFinanceFiles(financePaymentSaveFileReqVO);
        return success(true);
    }

    @PutMapping("/review")
    @Operation(summary = "提交审批")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:finance-payment:update')")
    public CommonResult<Boolean> submitReviewPurchasePlan(@RequestParam("id") Long id) {
        financePaymentService.submitReview(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除付款单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:finance-payment:delete')")
    public CommonResult<Boolean> deleteFinancePayment(@RequestParam("id") Long id) {
        financePaymentService.deleteFinancePayment(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得付款单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:finance-payment:query')")
    public CommonResult<FinancePaymentRespVO> getFinancePayment(@RequestParam("id") Long id) {
        FinancePaymentDO financePayment = financePaymentService.getFinancePayment(id);

        // 需要统计出关联的PO采购单，已付金额是多少
        List<FinancePaymentItemDO> financePaymentItemList = financePaymentService.getFinancePaymentItemListByPaymentId(financePayment.getId());
        List<Long> poIds = financePaymentItemList.stream().map(FinancePaymentItemDO::getBizId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDO> purchaseOrderList = purchaseOrderService.getPurchaseOrderList(poIds);
        Map<Long, BigDecimal> poPaidPriceMap = CollectionUtils.convertMap(purchaseOrderList, PurchaseOrderDO::getId, PurchaseOrderDO::getPaymentPrice);

        return success(BeanUtils.toBean(financePayment, FinancePaymentRespVO.class, financePaymentRespVO -> {
            financePaymentRespVO.setFinancePaymentItems(BeanUtils.toBean(financePaymentItemList, FinancePaymentItemRespVO.class,
                    financePaymentItemRespVO -> MapUtils.findAndThen(poPaidPriceMap, financePaymentItemRespVO.getBizId(), financePaymentItemRespVO::setPaidPrice)));
            financePaymentRespVO.setOwnerName(adminUserApi.getUser(Long.valueOf(financePayment.getOwner())).getCheckedData().getNickname());
        }));
    }

    @GetMapping("/page")
    @Operation(summary = "获得付款单分页")
    @PreAuthorize("@ss.hasPermission('dm:finance-payment:query')")
    public CommonResult<PageResult<FinancePaymentRespVO>> getFinancePaymentPage(@Valid FinancePaymentPageReqVO pageReqVO) {
        PageResult<FinancePaymentDO> pageResult = financePaymentService.getFinancePaymentPage(pageReqVO);

        // 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(CollectionUtils.convertListByFlatMap(pageResult.getList(),
                financePaymentDO -> Stream.of(NumberUtils.parseLong(financePaymentDO.getOwner()))));

        return success(BeanUtils.toBean(pageResult, FinancePaymentRespVO.class, financePaymentRespVO -> {
            MapUtils.findAndThen(userMap, financePaymentRespVO.getOwner(), user -> financePaymentRespVO.setOwnerName(user.getNickname()));
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出付款单 Excel")
    @PreAuthorize("@ss.hasPermission('dm:finance-payment:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinancePaymentExcel(@Valid FinancePaymentPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FinancePaymentDO> list = financePaymentService.getFinancePaymentPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "付款单.xls", "数据", FinancePaymentRespVO.class,
                BeanUtils.toBean(list, FinancePaymentRespVO.class));
    }

    // ==================== 子表（ERP 付款项） ====================

    @GetMapping("/finance-payment-item/list-by-payment-id")
    @Operation(summary = "获得ERP 付款项列表")
    @Parameter(name = "paymentId", description = "付款单编号")
    @PreAuthorize("@ss.hasPermission('dm:finance-payment:query')")
    public CommonResult<List<FinancePaymentItemRespVO>> getFinancePaymentItemListByPaymentId(@RequestParam("paymentId") Long paymentId) {
        // 需要统计出关联的PO采购单，已付金额是多少
        List<FinancePaymentItemDO> financePaymentItemList = financePaymentService.getFinancePaymentItemListByPaymentId(paymentId);
        List<Long> poIds = financePaymentItemList.stream().map(FinancePaymentItemDO::getBizId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDO> purchaseOrderList = purchaseOrderService.getPurchaseOrderList(poIds);
        Map<Long, BigDecimal> poPaidPriceMap = CollectionUtils.convertMap(purchaseOrderList, PurchaseOrderDO::getId, PurchaseOrderDO::getPaymentPrice);

        return success(BeanUtils.toBean(financePaymentItemList, FinancePaymentItemRespVO.class,
                financePaymentItemRespVO -> MapUtils.findAndThen(poPaidPriceMap, financePaymentItemRespVO.getBizId(), financePaymentItemRespVO::setPaidPrice)));
    }

}