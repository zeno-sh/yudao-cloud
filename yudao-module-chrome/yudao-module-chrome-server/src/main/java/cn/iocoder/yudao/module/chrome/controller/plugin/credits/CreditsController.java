package cn.iocoder.yudao.module.chrome.controller.plugin.credits;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.chrome.controller.plugin.credits.vo.ConsumeCreditsReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.credits.vo.ConsumeCreditsRespVO;
import cn.iocoder.yudao.module.chrome.enums.FeatureTypeEnum;
import cn.iocoder.yudao.module.chrome.service.credits.UserCreditsService;
import cn.iocoder.yudao.module.chrome.service.usage.UsageRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.CREDITS_INSUFFICIENT;

/**
 * Chrome插件 - 积分管理 Controller
 *
 * @author Jax
 */
@Tag(name = "Chrome插件 - 积分管理")
@RestController
@RequestMapping("/chrome/credits")
@Slf4j
public class CreditsController {

    @Resource
    private UserCreditsService userCreditsService;

    @Resource
    private UsageRecordService usageRecordService;
    

    @PostMapping("/consume")
    @Operation(summary = "消耗积分")
    @ApiAccessLog(operateType = OperateTypeEnum.GET)
    public CommonResult<ConsumeCreditsRespVO> consumeCredits(@Valid @RequestBody ConsumeCreditsReqVO reqVO) {

        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            log.warn("[CreditsController][consumeCredits] 用户不存在，邮箱: {}", reqVO.getEmail());
            return error(404, "用户不存在");
        }

        // 2. 验证功能类型并获取对应的积分消耗
        FeatureTypeEnum featureType;
        try {
            featureType = FeatureTypeEnum.valueOf(reqVO.getFeatureType());
        } catch (IllegalArgumentException e) {
            log.warn("[CreditsController][consumeCredits] 无效的功能类型: {}", reqVO.getFeatureType());
            return error(400, "无效的功能类型");
        }

        // 3. 计算需要消耗的积分
        Integer creditsToConsume = getCreditsForFeatureType(featureType);

        // 4. 检查积分余额
        if (!userCreditsService.hasEnoughCredits(userId, creditsToConsume)) {
            log.warn("[CreditsController][consumeCredits] 用户积分不足，用户ID: {}, 需要积分: {}",
                    userId, creditsToConsume);
            return error(CREDITS_INSUFFICIENT);
        }

        // 5. 消耗积分
        String businessId = generateBusinessId(userId, featureType);
        boolean consumeSuccess = userCreditsService.consumeCredits(
                userId,
                creditsToConsume,
                featureType.getType(),
                businessId
        );

        if (!consumeSuccess) {
            log.error("[CreditsController][consumeCredits] 积分消耗失败，用户ID: {}, 积分: {}",
                    userId, creditsToConsume);
            return error(500, "积分消耗失败");
        }

        // 6. 记录使用记录
        try {
            usageRecordService.recordUsage(userId, featureType.getType(), creditsToConsume, null);
            log.info("[CreditsController][consumeCredits] 使用记录已保存，用户ID: {}, 功能类型: {}",
                    userId, featureType.getType());
        } catch (Exception e) {
            log.error("[CreditsController][consumeCredits] 使用记录保存失败，但不影响主流程，用户ID: {}",
                    userId, e);
        }

        // 7. 获取剩余积分
        Integer remainingCredits = userCreditsService.getRemainingCredits(userId);

        // 8. 构建响应
        ConsumeCreditsRespVO respVO = new ConsumeCreditsRespVO();
        respVO.setSuccess(true);
        respVO.setCreditsConsumed(creditsToConsume);
        respVO.setRemainingCredits(remainingCredits);
        respVO.setFeatureTypeName(featureType.getName());
        respVO.setMessage("积分消耗成功");

        return success(respVO);

    }

    /**
     * 根据功能类型获取需要消耗的积分数
     * 只处理商品采集、排名采集、飞书导出、Excel导出
     * 其他功能通过AOP处理
     */
    private Integer getCreditsForFeatureType(FeatureTypeEnum featureType) {
        switch (featureType) {
            case PRODUCT_COLLECT:
                return 3;  // 商品采集消耗3积分
            case COMMENT_COLLECT:
                return 2;  // 评论采集消耗2积分
            case RANKING_COLLECT:
                return 1;  // 排名采集消耗1积分
            case FEISHU_EXPORT:
                return 30; // 飞书导出消耗30积分
            case EXCEL_EXPORT:
                return 15;  // Excel导出消耗15积分
            default:
                // 其他功能类型不在此接口处理范围内
                throw new IllegalArgumentException("不支持的功能类型: " + featureType.getName() + "，该功能通过其他方式处理");
        }
    }

    @GetMapping("/check")
    @Operation(summary = "检查积分是否足够")
    @ApiAccessLog(operateType = OperateTypeEnum.GET)
    public CommonResult<Boolean> checkCreditsSufficient(@RequestParam("featureType") Integer featureType) {

        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            log.warn("[CreditsController][checkCreditsSufficient] 用户未登录");
            return error(401, "用户未登录");
        }

        // 验证功能类型
        FeatureTypeEnum featureTypeEnum;
        try {
            featureTypeEnum = FeatureTypeEnum.valueOf(featureType);
        } catch (IllegalArgumentException e) {
            log.warn("[CreditsController][checkCreditsSufficient] 无效的功能类型: {}", featureType, e);
            return error(400, "无效的功能类型");
        }

        // 获取该功能需要消耗的积分数
        Integer creditsRequired = getCreditsForFeatureType(featureTypeEnum);

        // 检查积分是否足够
        boolean sufficient = userCreditsService.hasEnoughCredits(userId, creditsRequired);
        return success(sufficient);
    }

    /**
     * 生成业务ID
     */
    private String generateBusinessId(Long userId, FeatureTypeEnum featureType) {
        return String.format("credits_consume_%d_%s_%d", userId, featureType.name(), System.currentTimeMillis());
    }
}
