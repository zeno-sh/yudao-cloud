package cn.iocoder.yudao.module.chrome.controller.admin.order;

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

import cn.iocoder.yudao.module.chrome.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.user.ChromeUserMapper;
import cn.iocoder.yudao.module.chrome.dal.mysql.plan.SubscriptionPlanMapper;
import cn.iocoder.yudao.module.chrome.service.order.SubscriptionOrderService;
import cn.iocoder.yudao.module.chrome.service.user.UserService;
import cn.iocoder.yudao.module.chrome.service.plan.SubscriptionPlanService;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - 订阅订单")
@RestController
@RequestMapping("/chrome/subscription-order")
@Validated
public class SubscriptionOrderController {

    @Resource
    private SubscriptionOrderService subscriptionOrderService;
    @Resource
    private UserService userService;
    @Resource
    private SubscriptionPlanService subscriptionPlanService;
    @Resource
    private ChromeUserMapper chromeUserMapper;
    @Resource
    private SubscriptionPlanMapper subscriptionPlanMapper;

    @PostMapping("/create")
    @Operation(summary = "创建订阅订单")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-order:create')")
    public CommonResult<Long> createSubscriptionOrder(@Valid @RequestBody SubscriptionOrderSaveReqVO createReqVO) {
        return success(subscriptionOrderService.createSubscriptionOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新订阅订单")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-order:update')")
    public CommonResult<Boolean> updateSubscriptionOrder(@Valid @RequestBody SubscriptionOrderSaveReqVO updateReqVO) {
        subscriptionOrderService.updateSubscriptionOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除订阅订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('chrome:subscription-order:delete')")
    public CommonResult<Boolean> deleteSubscriptionOrder(@RequestParam("id") Long id) {
        subscriptionOrderService.deleteSubscriptionOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得订阅订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-order:query')")
    public CommonResult<SubscriptionOrderRespVO> getSubscriptionOrder(@RequestParam("id") Long id) {
        SubscriptionOrderDO subscriptionOrder = subscriptionOrderService.getSubscriptionOrder(id);
        SubscriptionOrderRespVO respVO = BeanUtils.toBean(subscriptionOrder, SubscriptionOrderRespVO.class);
        
        // 填充用户邮箱
        if (subscriptionOrder.getUserId() != null) {
            UserDO user = userService.getUser(subscriptionOrder.getUserId());
            if (user != null) {
                respVO.setUserEmail(user.getEmail());
            }
        }
        
        // 填充套餐名称
        if (subscriptionOrder.getPlanId() != null) {
            SubscriptionPlanDO plan = subscriptionPlanService.getSubscriptionPlan(subscriptionOrder.getPlanId());
            if (plan != null) {
                respVO.setPlanName(plan.getPlanName());
            }
        }
        
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得订阅订单分页")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-order:query')")
    public CommonResult<PageResult<SubscriptionOrderRespVO>> getSubscriptionOrderPage(@Valid SubscriptionOrderPageReqVO pageReqVO) {
        PageResult<SubscriptionOrderDO> pageResult = subscriptionOrderService.getSubscriptionOrderPage(pageReqVO);
        List<SubscriptionOrderRespVO> respList = BeanUtils.toBean(pageResult.getList(), SubscriptionOrderRespVO.class);
        
        // 批量填充用户邮箱
        Set<Long> userIds = respList.stream().map(SubscriptionOrderRespVO::getUserId).collect(java.util.stream.Collectors.toSet());
        Map<Long, UserDO> userMap = convertMap(chromeUserMapper.selectBatchIds(userIds), UserDO::getId);
        
        // 批量填充套餐名称
        Set<Long> planIds = respList.stream().map(SubscriptionOrderRespVO::getPlanId).collect(java.util.stream.Collectors.toSet());
        Map<Long, SubscriptionPlanDO> planMap = convertMap(subscriptionPlanMapper.selectBatchIds(planIds), SubscriptionPlanDO::getId);
        
        // 填充数据
        respList.forEach(respVO -> {
            UserDO user = userMap.get(respVO.getUserId());
            if (user != null) {
                respVO.setUserEmail(user.getEmail());
            }
            SubscriptionPlanDO plan = planMap.get(respVO.getPlanId());
            if (plan != null) {
                respVO.setPlanName(plan.getPlanName());
            }
        });
        
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出订阅订单 Excel")
    @PreAuthorize("@ss.hasPermission('chrome:subscription-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSubscriptionOrderExcel(@Valid SubscriptionOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SubscriptionOrderDO> list = subscriptionOrderService.getSubscriptionOrderPage(pageReqVO).getList();
        List<SubscriptionOrderRespVO> respList = BeanUtils.toBean(list, SubscriptionOrderRespVO.class);
        
        // 批量填充用户邮箱和套餐名称
        Set<Long> userIds = respList.stream().map(SubscriptionOrderRespVO::getUserId).collect(java.util.stream.Collectors.toSet());
        Map<Long, UserDO> userMap = convertMap(chromeUserMapper.selectBatchIds(userIds), UserDO::getId);
        Set<Long> planIds = respList.stream().map(SubscriptionOrderRespVO::getPlanId).collect(java.util.stream.Collectors.toSet());
        Map<Long, SubscriptionPlanDO> planMap = convertMap(subscriptionPlanMapper.selectBatchIds(planIds), SubscriptionPlanDO::getId);
        
        respList.forEach(respVO -> {
            UserDO user = userMap.get(respVO.getUserId());
            if (user != null) {
                respVO.setUserEmail(user.getEmail());
            }
            SubscriptionPlanDO plan = planMap.get(respVO.getPlanId());
            if (plan != null) {
                respVO.setPlanName(plan.getPlanName());
            }
        });
        
        // 导出 Excel
        ExcelUtils.write(response, "订阅订单.xls", "数据", SubscriptionOrderRespVO.class, respList);
    }

}