package cn.iocoder.yudao.module.chrome.controller.admin.subscription;

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

import cn.iocoder.yudao.module.chrome.controller.admin.subscription.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.service.subscription.SubscriptionService;

@Tag(name = "管理后台 - 插件订阅")
@RestController
@RequestMapping("/chrome/subscription")
@Validated
public class SubscriptionController {

    @Resource
    private SubscriptionService subscriptionService;

    @PostMapping("/create")
    @Operation(summary = "创建插件订阅")
    @PreAuthorize("@ss.hasPermission('chrome:subscription:create')")
    public CommonResult<Long> createSubscription(@Valid @RequestBody SubscriptionSaveReqVO createReqVO) {
        return success(subscriptionService.createSubscription(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新插件订阅")
    @PreAuthorize("@ss.hasPermission('chrome:subscription:update')")
    public CommonResult<Boolean> updateSubscription(@Valid @RequestBody SubscriptionSaveReqVO updateReqVO) {
        subscriptionService.updateSubscription(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除插件订阅")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('chrome:subscription:delete')")
    public CommonResult<Boolean> deleteSubscription(@RequestParam("id") Long id) {
        subscriptionService.deleteSubscription(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得插件订阅")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('chrome:subscription:query')")
    public CommonResult<SubscriptionRespVO> getSubscription(@RequestParam("id") Long id) {
        SubscriptionDO subscription = subscriptionService.getSubscription(id);
        return success(BeanUtils.toBean(subscription, SubscriptionRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得插件订阅分页")
    @PreAuthorize("@ss.hasPermission('chrome:subscription:query')")
    public CommonResult<PageResult<SubscriptionRespVO>> getSubscriptionPage(@Valid SubscriptionPageReqVO pageReqVO) {
        PageResult<SubscriptionDO> pageResult = subscriptionService.getSubscriptionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SubscriptionRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出插件订阅 Excel")
    @PreAuthorize("@ss.hasPermission('chrome:subscription:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSubscriptionExcel(@Valid SubscriptionPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SubscriptionDO> list = subscriptionService.getSubscriptionPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "插件订阅.xls", "数据", SubscriptionRespVO.class,
                        BeanUtils.toBean(list, SubscriptionRespVO.class));
    }

}