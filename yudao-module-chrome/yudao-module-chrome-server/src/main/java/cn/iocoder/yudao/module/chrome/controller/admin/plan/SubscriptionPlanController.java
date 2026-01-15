package cn.iocoder.yudao.module.chrome.controller.admin.plan;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.chrome.controller.admin.plan.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.module.chrome.service.plan.SubscriptionPlanService;

@Tag(name = "管理后台 - 订阅套餐配置")
@RestController
@RequestMapping("/chrome/subscription-plan")
@Validated
public class SubscriptionPlanController {

    @Resource
    private SubscriptionPlanService subscriptionPlanService;

    @PostMapping("/create")
    @Operation(summary = "创建订阅套餐配置")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-plan:create')")
    public CommonResult<Long> createSubscriptionPlan(@Valid @RequestBody SubscriptionPlanSaveReqVO createReqVO) {
        return success(subscriptionPlanService.createSubscriptionPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新订阅套餐配置")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-plan:update')")
    public CommonResult<Boolean> updateSubscriptionPlan(@Valid @RequestBody SubscriptionPlanSaveReqVO updateReqVO) {
        subscriptionPlanService.updateSubscriptionPlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除订阅套餐配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('chrome:subscription-plan:delete')")
    public CommonResult<Boolean> deleteSubscriptionPlan(@RequestParam("id") Long id) {
        subscriptionPlanService.deleteSubscriptionPlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得订阅套餐配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-plan:query')")
    public CommonResult<SubscriptionPlanRespVO> getSubscriptionPlan(@RequestParam("id") Long id) {
        SubscriptionPlanDO subscriptionPlan = subscriptionPlanService.getSubscriptionPlan(id);
        return success(BeanUtils.toBean(subscriptionPlan, SubscriptionPlanRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得订阅套餐配置分页")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-plan:query')")
    public CommonResult<PageResult<SubscriptionPlanRespVO>> getSubscriptionPlanPage(@Valid SubscriptionPlanPageReqVO pageReqVO) {
        PageResult<SubscriptionPlanDO> pageResult = subscriptionPlanService.getSubscriptionPlanPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SubscriptionPlanRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出订阅套餐配置 Excel")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSubscriptionPlanExcel(@Valid SubscriptionPlanPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SubscriptionPlanDO> list = subscriptionPlanService.getSubscriptionPlanPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "订阅套餐配置.xls", "数据", SubscriptionPlanRespVO.class,
                        BeanUtils.toBean(list, SubscriptionPlanRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有订阅套餐配置")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-plan:query')")
    public CommonResult<List<SubscriptionPlanRespVO>> getAllSubscriptionPlans() {
        List<SubscriptionPlanDO> list = subscriptionPlanService.getAllSubscriptionPlans();
        return success(BeanUtils.toBean(list, SubscriptionPlanRespVO.class));
    }

}