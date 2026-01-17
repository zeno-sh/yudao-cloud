package cn.iocoder.yudao.module.chrome.controller.plugin.referral;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo.ChromeReferralInfoRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo.ChromeReferralRecordPageReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo.ChromeReferralRecordRespVO;
import cn.iocoder.yudao.module.chrome.service.referral.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Chrome插件 - 推广分销 Controller
 */
@Tag(name = "Chrome插件 - 推广分销")
@RestController
@RequestMapping("/chrome/referral")
@Validated
public class ReferralController {

    @Resource
    private ReferralService referralService;

    @PostMapping("/apply")
    @Operation(summary = "申请/获取我的推广码")
    public CommonResult<String> applyReferralCode() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String code = referralService.generateReferralCode(userId);
        return success(code);
    }

    @GetMapping("/info")
    @Operation(summary = "获取我的推广信息")
    public CommonResult<ChromeReferralInfoRespVO> getReferralInfo() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(referralService.getReferralInfo(userId));
    }

    @GetMapping("/records")
    @Operation(summary = "分页查询推广佣金记录")
    public CommonResult<PageResult<ChromeReferralRecordRespVO>> getReferralRecordPage(
            @Validated ChromeReferralRecordPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(referralService.getReferralRecordPage(reqVO, userId));
    }

}
