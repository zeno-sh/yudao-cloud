package cn.iocoder.yudao.module.chrome.controller.plugin.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo.CreditsTransactionPageReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.user.vo.UserInfoRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.user.vo.UserCreditsRecordPageReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.user.vo.UserCreditsRecordRespVO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.transaction.CreditsTransactionDO;
import cn.iocoder.yudao.module.chrome.enums.TransactionTypeEnum;
import cn.iocoder.yudao.module.chrome.service.user.UserService;
import cn.iocoder.yudao.module.chrome.service.transaction.CreditsTransactionService;
import cn.iocoder.yudao.module.chrome.service.order.SubscriptionOrderService;
import cn.iocoder.yudao.module.chrome.service.plan.SubscriptionPlanService;
import cn.iocoder.yudao.module.chrome.controller.plugin.user.vo.UserRechargeRecordRespVO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Chrome插件 - 用户信息 Controller
 *
 * @author Jax
 */
@Tag(name = "Chrome插件 - 用户信息")
@RestController
@RequestMapping("/chrome/user-info")
@Slf4j
public class UserInfoController {

    @Resource
    private UserService userService;

    @Resource
    private CreditsTransactionService creditsTransactionService;

    @Resource
    private SubscriptionOrderService subscriptionOrderService;

    @Resource
    private SubscriptionPlanService subscriptionPlanService;

    @GetMapping("/get")
    @Operation(summary = "获取当前用户信息")
    public CommonResult<UserInfoRespVO> getCurrentUserInfo() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return CommonResult.error(401, "用户未登录");
        }

        UserInfoRespVO userInfo = userService.getUserInfo(userId);
        return success(userInfo);
    }

    @GetMapping("/get-by-email")
    @Operation(summary = "根据邮箱获取用户信息")
    public CommonResult<UserInfoRespVO> getUserInfoByEmail(@RequestParam("email") String email) {
        UserInfoRespVO userInfo = userService.getUserInfoByEmail(email);
        return success(userInfo);
    }

    @GetMapping("/credits-records")
    @Operation(summary = "分页查询当前用户积分记录")
    public CommonResult<PageResult<UserCreditsRecordRespVO>> getUserCreditsRecords(
            @Valid UserCreditsRecordPageReqVO pageReqVO) {
        // 获取当前登录用户ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return CommonResult.error(401, "用户未登录");
        }

        // 构建查询条件
        CreditsTransactionPageReqVO transactionPageReqVO = new CreditsTransactionPageReqVO();
        transactionPageReqVO.setPageNo(pageReqVO.getPageNo());
        transactionPageReqVO.setPageSize(pageReqVO.getPageSize());
        transactionPageReqVO.setTransactionType(pageReqVO.getTransactionType());
        transactionPageReqVO.setBusinessType(pageReqVO.getBusinessType());
        transactionPageReqVO.setCreateTime(pageReqVO.getCreateTime());

        // 查询积分交易记录
        PageResult<CreditsTransactionDO> pageResult = creditsTransactionService
                .getCreditsTransactionPageByUserId(userId, transactionPageReqVO);

        // 转换为响应VO
        PageResult<UserCreditsRecordRespVO> result = BeanUtils.toBean(pageResult, UserCreditsRecordRespVO.class);

        // 设置交易类型名称
        result.getList().forEach(record -> {
            if (record.getTransactionType() != null) {
                try {
                    TransactionTypeEnum typeEnum = TransactionTypeEnum.valueOf(record.getTransactionType());
                    record.setTransactionTypeName(typeEnum.getDesc());
                } catch (IllegalArgumentException e) {
                    record.setTransactionTypeName("未知类型");
                }
            }
        });

        return success(result);
    }

    @GetMapping("/recharge-records")
    @Operation(summary = "分页查询当前用户充值记录")
    public CommonResult<PageResult<UserRechargeRecordRespVO>> getUserRechargeRecords(@Valid PageParam pageReqVO) {
        // 获取当前登录用户ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return CommonResult.error(401, "用户未登录");
        }

        // 查询已支付订单
        PageResult<SubscriptionOrderDO> pageResult = subscriptionOrderService.getPaidOrdersByUserId(userId, pageReqVO);

        // 批量获取套餐信息以填充名称
        Set<Long> planIds = new HashSet<>();
        pageResult.getList().forEach(order -> {
            if (order.getPlanId() != null) {
                planIds.add(order.getPlanId());
            }
        });
        Map<Long, SubscriptionPlanDO> planMap = new HashMap<>();
        if (!planIds.isEmpty()) {
            List<SubscriptionPlanDO> plans = subscriptionPlanService.getSubscriptionPlanList(planIds);
            plans.forEach(plan -> planMap.put(plan.getId(), plan));
        }

        // 转换为响应VO
        PageResult<UserRechargeRecordRespVO> result = BeanUtils.toBean(pageResult, UserRechargeRecordRespVO.class);
        result.getList().forEach(vo -> {
            // 找到对应的订单（通过ID匹配，这里有点绕因为BeanUtils转换了，但顺序应该一致，更好是遍历DO列表或者通过ID查找）
            // 修正：BeanUtils.toBean转换后，vo的id来自order的id
            SubscriptionOrderDO order = pageResult.getList().stream().filter(o -> o.getId().equals(vo.getId()))
                    .findFirst().orElse(null);
            if (order != null && order.getPlanId() != null) {
                SubscriptionPlanDO plan = planMap.get(order.getPlanId());
                if (plan != null) {
                    vo.setPlanName(plan.getPlanName());
                } else {
                    vo.setPlanName("未知套餐");
                }
            } else {
                vo.setPlanName("未知套餐");
            }
            // 针对积分包或特殊处理，如果planId为空但有subscriptionType，也可以处理，但这里主要依赖planId
            vo.setPayPrice(order.getActualPrice());
        });

        return success(result);
    }
}
