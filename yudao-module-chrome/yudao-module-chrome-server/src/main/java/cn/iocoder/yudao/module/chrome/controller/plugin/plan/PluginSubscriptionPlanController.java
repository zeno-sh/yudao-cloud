package cn.iocoder.yudao.module.chrome.controller.plugin.plan;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.chrome.service.plan.SubscriptionPlanService;
import cn.iocoder.yudao.module.chrome.service.config.ChromeConfigService;
import cn.iocoder.yudao.module.chrome.service.credits.CreditsPackService;
import cn.iocoder.yudao.module.chrome.controller.plugin.plan.vo.SubscriptionPlanListRespVO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.module.chrome.enums.SubscriptionTypeEnum;
import cn.iocoder.yudao.module.chrome.enums.BillingCycleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Chrome插件 - 订阅套餐 Controller
 *
 * @author Jax
 */
@Tag(name = "Chrome插件 - 订阅套餐")
@RestController
@RequestMapping("/chrome/subscription-plan")
@Slf4j
public class PluginSubscriptionPlanController {

    @Resource
    private SubscriptionPlanService subscriptionPlanService;

    @Resource
    private ChromeConfigService chromeConfigService;

    @Resource
    private CreditsPackService creditsPackService;

    @GetMapping("/list")
    @Operation(summary = "获取套餐列表")
    @ApiAccessLog(operateType = OperateTypeEnum.GET)
    @PermitAll
    public CommonResult<List<SubscriptionPlanListRespVO>> getSubscriptionPlanList() {
        List<SubscriptionPlanDO> plans = subscriptionPlanService.getEnabledSubscriptionPlans();
        // 转换为VO并按排序字段排序
        List<SubscriptionPlanListRespVO> respList = BeanUtils.toBean(plans, SubscriptionPlanListRespVO.class);
        respList.sort(Comparator.comparing(SubscriptionPlanListRespVO::getSortOrder,
                Comparator.nullsLast(Comparator.naturalOrder())));

        log.info("[getSubscriptionPlanList][返回套餐数量: {}]", respList.size());
        return success(respList);
    }
}
