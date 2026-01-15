package cn.iocoder.yudao.module.chrome.controller.plugin.auth;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthLoginReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthLoginRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthRegisterReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthRegisterRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthResetPasswordReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthSendEmailCodeReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthUpdatePasswordReqVO;
import cn.iocoder.yudao.module.chrome.service.auth.ChromeAuthService;
import cn.iocoder.yudao.module.chrome.service.email.EmailCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Chrome认证 Controller
 *
 * @author Jax
 */
@Tag(name = "Chrome认证")
@RestController
@RequestMapping("/chrome/auth")
@Validated
public class ChromeAuthController {

    @Resource
    private ChromeAuthService chromeAuthService;

    @Resource
    private EmailCodeService emailCodeService;

    @PostMapping("/login")
    @Operation(summary = "使用邮箱密码登录")
    @PermitAll
    public CommonResult<ChromeAuthLoginRespVO> login(@RequestBody @Valid ChromeAuthLoginReqVO reqVO,
                                                     HttpServletRequest request) {
        String clientIP = getClientIP(request);
        String userAgent = getUserAgent(request);
        return success(chromeAuthService.login(reqVO, clientIP, userAgent));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    @PermitAll
    public CommonResult<ChromeAuthRegisterRespVO> register(@RequestBody @Valid ChromeAuthRegisterReqVO reqVO,
                                                           HttpServletRequest request) {
        String clientIP = getClientIP(request);
        String userAgent = getUserAgent(request);
        return success(chromeAuthService.register(reqVO, clientIP, userAgent));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出系统")
    @PermitAll
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = chromeAuthService.getAccessToken(request);
        if (token != null) {
            chromeAuthService.logout(token);
        }
        return success(true);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌")
    @Parameter(name = "refreshToken", description = "刷新令牌", required = true)
    @PermitAll
    public CommonResult<ChromeAuthLoginRespVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(chromeAuthService.refreshToken(refreshToken));
    }

    @GetMapping("/get-permission-info")
    @Operation(summary = "获取登录用户的权限信息")
    public CommonResult<ChromeAuthLoginRespVO> getPermissionInfo() {
        return success(chromeAuthService.getPermissionInfo());
    }

    @PostMapping("/reset-password")
    @Operation(summary = "重置密码")
    @PermitAll
    public CommonResult<Boolean> resetPassword(@RequestBody @Valid ChromeAuthResetPasswordReqVO reqVO,
                                               HttpServletRequest request) {
        String clientIP = getClientIP(request);
        String userAgent = getUserAgent(request);
        chromeAuthService.resetPassword(reqVO, clientIP, userAgent);
        return success(true);
    }

    @PostMapping("/update-password")
    @Operation(summary = "修改密码")
    @PermitAll
    public CommonResult<Boolean> updatePassword(@RequestBody @Valid ChromeAuthUpdatePasswordReqVO reqVO,
                                                HttpServletRequest request) {
        String clientIP = getClientIP(request);
        String userAgent = getUserAgent(request);
        chromeAuthService.updatePassword(reqVO, clientIP, userAgent);
        return success(true);
    }

    @PostMapping("/send-code")
    @Operation(summary = "发送邮箱验证码")
    @PermitAll
    public CommonResult<Boolean> sendEmailCode(@RequestBody @Valid ChromeAuthSendEmailCodeReqVO reqVO,
                                               HttpServletRequest request) {
        String clientIP = getClientIP(request);
        emailCodeService.sendEmailCode(reqVO.getEmail(), reqVO.getScene(), clientIP);
        return success(true);
    }

    private static String getClientIP(HttpServletRequest request) {
        String xip = request.getHeader("X-Real-IP");
        String xfor = request.getHeader("X-Forwarded-For");
        if (xfor != null && !xfor.isEmpty() && !"unknown".equalsIgnoreCase(xfor)) {
            int index = xfor.indexOf(",");
            if (index != -1) {
                return xfor.substring(0, index);
            } else {
                return xfor;
            }
        }
        xfor = xip;
        if (xfor != null && !xfor.isEmpty() && !"unknown".equalsIgnoreCase(xfor)) {
            return xfor;
        }
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("Proxy-Client-IP");
        }
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getRemoteAddr();
        }
        return xfor;
    }

    private static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

}