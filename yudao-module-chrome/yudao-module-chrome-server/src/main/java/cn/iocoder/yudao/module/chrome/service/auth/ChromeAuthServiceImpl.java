package cn.iocoder.yudao.module.chrome.service.auth;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthLoginReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthLoginRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthRegisterReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthRegisterRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthResetPasswordReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthUpdatePasswordReqVO;
import cn.iocoder.yudao.module.chrome.controller.admin.user.vo.UserSaveReqVO;
import cn.iocoder.yudao.module.chrome.service.email.EmailCodeService;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;

import cn.iocoder.yudao.module.chrome.service.subscription.SubscriptionService;
import cn.iocoder.yudao.module.chrome.service.user.UserService;
import cn.iocoder.yudao.module.chrome.service.credits.UserCreditsService;
import cn.iocoder.yudao.module.chrome.enums.SubscriptionTypeEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.api.logger.LoginLogApi;
import cn.iocoder.yudao.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;

import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AUTH_LOGIN_BAD_CREDENTIALS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED;

/**
 * Chrome认证 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
@Slf4j
public class ChromeAuthServiceImpl implements ChromeAuthService {

    @Resource
    private UserService userService;

    @Resource
    private SubscriptionService subscriptionService;

    @Resource
    private OAuth2TokenCommonApi oauth2TokenApi;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private LoginLogApi loginLogApi;

    @Resource
    private EmailCodeService emailCodeService;

    @Resource
    private UserCreditsService userCreditsService;

    @Override
    public UserDO authenticate(String email, String password) {
        final LoginLogTypeEnum logTypeEnum = LoginLogTypeEnum.LOGIN_USERNAME;
        // 校验账号是否存在
        UserDO user = userService.getUserByEmail(email);
        if (user == null) {
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        if (!isPasswordMatch(password, user.getPassword())) {
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 校验是否禁用
        if (!user.getStatus()) {
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }
        return user;
    }

    @Override
    public ChromeAuthLoginRespVO login(@Valid ChromeAuthLoginReqVO reqVO, String clientIP, String userAgent) {

        UserDO user = authenticate(reqVO.getEmail(), reqVO.getPassword());

        // 单设备登录限制：如果提供了新的设备令牌，则踢出其他设备
        if (StrUtil.isNotBlank(reqVO.getDeviceToken())) {
            this.enforceDeviceLimit(user.getId(), reqVO.getDeviceToken());
        }

        // 更新用户的登录信息
        this.updateUserLogin(user.getId(), clientIP);

        // 更新用户设备令牌
        if (StrUtil.isNotBlank(reqVO.getDeviceToken())) {
            this.updateUserDeviceToken(user.getId(), reqVO.getDeviceToken());
        }

        // 创建 Token 令牌，记录登录日志
        return this.createTokenAfterLoginSuccess(user.getId(), clientIP, userAgent);
    }

    private void updateUserLogin(Long userId, String clientIP) {
        UserDO updateObj = new UserDO();
        updateObj.setId(userId);
        updateObj.setLoginIp(clientIP);
        updateObj.setLoginDate(LocalDateTime.now());
        userService.updateUser(updateObj);
    }

    private void updateUserDeviceToken(Long userId, String deviceToken) {
        UserDO updateObj = new UserDO();
        updateObj.setId(userId);
        updateObj.setDeviceToken(deviceToken);
        userService.updateUser(updateObj);
    }

    /**
     * 强制单设备登录限制
     * 如果用户当前设备令牌与新设备令牌不同，则踢出旧设备的所有token
     *
     * @param userId         用户ID
     * @param newDeviceToken 新设备令牌
     */
    private void enforceDeviceLimit(Long userId, String newDeviceToken) {
        // 获取用户当前的设备令牌
        UserDO user = userService.getUser(userId);
        if (user == null) {
            return;
        }

        String currentDeviceToken = user.getDeviceToken();

        // 如果当前设备令牌存在且与新设备令牌不同，则踢出旧设备
        if (StrUtil.isNotBlank(currentDeviceToken) && !currentDeviceToken.equals(newDeviceToken)) {
            oauth2TokenApi.removeAccessTokenByUserId(userId, UserTypeEnum.PLUGIN.getValue());
            log.info("[enforceDeviceLimit][用户({}) 设备切换，旧设备令牌({}) 被踢出，新设备令牌({})]",
                    userId, currentDeviceToken, newDeviceToken);
        }
    }

    @Override
    public void logout(String token) {
        // 删除访问令牌
        OAuth2AccessTokenRespDTO accessTokenRespDTO = oauth2TokenApi.removeAccessToken(token).getData();
        if (accessTokenRespDTO == null) {
            return;
        }
        // 删除成功，则记录登出日志
        this.createLogoutLog(Long.valueOf(accessTokenRespDTO.getUserId()));
    }

    @Override
    public ChromeAuthLoginRespVO refreshToken(String refreshToken) {
        OAuth2AccessTokenRespDTO accessTokenDO = oauth2TokenApi.refreshAccessToken(refreshToken, "chrome-web")
                .getData();
        return ChromeAuthLoginRespVO.builder()
                .accessToken(accessTokenDO.getAccessToken())
                .refreshToken(accessTokenDO.getRefreshToken())
                .expiresTime(accessTokenDO.getExpiresTime())
                .build();
    }

    @Override
    public ChromeAuthLoginRespVO getPermissionInfo() {
        // 1.1 获得用户信息
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return null;
        }
        UserDO user = userService.getUser(loginUser.getId());
        if (user == null) {
            return null;
        }

        // 1.2 获得订阅信息
        SubscriptionDO subscription = subscriptionService
                .getActiveSubscriptionByUserId(Long.valueOf(loginUser.getId()));

        // 2. 拼接结果返回
        return ChromeAuthLoginRespVO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .subscriptionStatus(subscription != null ? (subscription.getStatus() ? 1 : 0) : null)
                .subscriptionType(subscription != null ? subscription.getSubscriptionType() : null)
                .subscriptionExpireTime(subscription != null ? subscription.getEndTime() : null)
                .build();
    }

    @Override
    public String getAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    @Override
    public ChromeAuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String clientIP, String userAgent) {
        // 1. 获得用户信息
        UserDO user = userService.getUser(userId);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 2. 获得订阅信息
        SubscriptionDO subscription = subscriptionService.getActiveSubscriptionByUserId(userId);

        // 3. 创建 LoginUser 对象
        LoginUser loginUser = new LoginUser();
        loginUser.setId(userId);
        loginUser.setUserType(UserTypeEnum.PLUGIN.getValue());

        // 设置用户信息到 info 中
        Map<String, String> info = new HashMap<>();
        info.put("email", user.getEmail());
        info.put("nickname", user.getNickname());
        info.put("deviceToken", user.getDeviceToken());
        info.put("subscriptionStatus", subscription != null ? subscription.getStatus().toString() : null);
        info.put("subscriptionType", subscription != null ? subscription.getSubscriptionType().toString() : null);
        info.put("subscriptionExpireTime", subscription != null ? subscription.getEndTime().toString() : null);
        loginUser.setInfo(info);

        // 4. 创建 Token 令牌
        OAuth2AccessTokenCreateReqDTO createReqDTO = new OAuth2AccessTokenCreateReqDTO();
        createReqDTO.setUserId(userId);
        createReqDTO.setUserType(UserTypeEnum.PLUGIN.getValue());
        createReqDTO.setClientId("chrome-web");
        createReqDTO.setScopes(null); // 不使用 scope 机制
        OAuth2AccessTokenRespDTO accessTokenRespDTO = oauth2TokenApi.createAccessToken(createReqDTO).getData();

        // 5. 记录登录日志
        ChromeAuthLoginRespVO respVO = ChromeAuthLoginRespVO.builder()
                .userId(userId)
                .accessToken(accessTokenRespDTO.getAccessToken())
                .refreshToken(accessTokenRespDTO.getRefreshToken())
                .expiresTime(accessTokenRespDTO.getExpiresTime())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .subscriptionStatus(subscription != null ? (subscription.getStatus() ? 1 : 0) : null)
                .subscriptionType(subscription != null ? subscription.getSubscriptionType() : null)
                .subscriptionExpireTime(subscription != null ? subscription.getEndTime() : null)
                .build();
        return respVO;
    }

    private void createLogoutLog(Long userId) {
        // 创建登出日志
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(LoginLogTypeEnum.LOGOUT_SELF.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(UserTypeEnum.PLUGIN.getValue());
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogApi.createLoginLog(reqDTO);

        log.info("[createLogoutLog][userId({})]", userId);
    }

    @Override
    public ChromeAuthRegisterRespVO register(ChromeAuthRegisterReqVO reqVO, String clientIP, String userAgent) {

        // 校验邮箱验证码
        emailCodeService.validateEmailCode(reqVO.getEmail(), reqVO.getEmailCode(), 10); // 10表示注册场景

        // 校验邮箱是否已存在
        if (userService.getUserByEmail(reqVO.getEmail()) != null) {
            throw exception(USER_EMAIL_EXISTS);
        }

        // 校验设备令牌是否已存在
        if (StrUtil.isNotBlank(reqVO.getDeviceToken()) && userService.isDeviceTokenExists(reqVO.getDeviceToken())) {
            throw exception(USER_DEVICE_TOKEN_EXISTS);
        }

        // 校验密码确认
        // if (!reqVO.getPassword().equals(reqVO.getConfirmPassword())) {
        // throw exception(PASSWORD_CONFIRM_NOT_MATCH);
        // }

        // 创建用户
        UserSaveReqVO userSaveReqVO = new UserSaveReqVO();
        userSaveReqVO.setEmail(reqVO.getEmail());
        userSaveReqVO.setPassword(passwordEncoder.encode(reqVO.getPassword()));
        userSaveReqVO.setNickname(reqVO.getNickname());
        userSaveReqVO.setDeviceToken(reqVO.getDeviceToken());
        userSaveReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus() == 0);

        Long userId = userService.createUser(userSaveReqVO);

        // 处理推广码逻辑
        if (StrUtil.isNotBlank(reqVO.getReferralCode())) {
            this.handleReferralBinding(userId, reqVO.getReferralCode());
        }

        // 获取创建的用户信息
        UserDO user = userService.getUser(userId);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 为新用户创建免费套餐和初始积分
        this.createFreeSubscriptionAndCredits(userId);

        // 更新用户的登录信息
        this.updateUserLogin(user.getId(), clientIP);

        // 创建 Token 令牌，记录登录日志
        ChromeAuthLoginRespVO loginResp = this.createTokenAfterLoginSuccess(user.getId(), clientIP, userAgent);

        return ChromeAuthRegisterRespVO.builder()
                .userId(loginResp.getUserId())
                .accessToken(loginResp.getAccessToken())
                .refreshToken(loginResp.getRefreshToken())
                .expiresTime(loginResp.getExpiresTime())
                .email(loginResp.getEmail())
                .nickname(user.getNickname())
                .subscriptionType(loginResp.getSubscriptionType())
                .subscriptionStatus(loginResp.getSubscriptionStatus())
                .subscriptionExpireTime(loginResp.getSubscriptionExpireTime())
                .build();
    }

    @Override
    public void resetPassword(ChromeAuthResetPasswordReqVO reqVO, String clientIP, String userAgent) {

        // 校验邮箱验证码
        emailCodeService.validateEmailCode(reqVO.getEmail(), reqVO.getEmailCode(), 20); // 20表示密码重置场景

        // 校验新密码和确认密码是否一致
        if (!reqVO.getNewPassword().equals(reqVO.getConfirmNewPassword())) {
            throw exception(PASSWORD_CONFIRM_NOT_MATCH);
        }

        // 查找用户
        UserDO user = userService.getUserByEmail(reqVO.getEmail());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 更新密码
        userService.updateUserPassword(user.getId(), passwordEncoder.encode(reqVO.getNewPassword()));

    }

    @Override
    public void updatePassword(ChromeAuthUpdatePasswordReqVO reqVO, String clientIP, String userAgent) {

        // 获取当前登录用户
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            throw exception(UNAUTHORIZED);
        }

        // 获取用户信息
        UserDO user = userService.getUser(loginUser.getId());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 校验旧密码
        if (!passwordEncoder.matches(reqVO.getOldPassword(), user.getPassword())) {
            throw exception(USER_PASSWORD_ERROR);
        }

        // 校验新密码和确认密码是否一致
        if (!reqVO.getNewPassword().equals(reqVO.getConfirmNewPassword())) {
            throw exception(PASSWORD_CONFIRM_NOT_MATCH);
        }

        // 校验邮箱验证码
        emailCodeService.validateEmailCode(user.getEmail(), reqVO.getEmailCode(), 30); // 30表示密码修改场景

        // 更新密码
        userService.updateUserPassword(user.getId(), passwordEncoder.encode(reqVO.getNewPassword()));
    }

    @Override
    public boolean isPasswordMatch(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 为新注册用户创建免费套餐和初始积分
     *
     * @param userId 用户ID
     */
    private void createFreeSubscriptionAndCredits(Long userId) {
        try {
            log.info("[createFreeSubscriptionAndCredits][为用户({})创建免费套餐和初始积分]", userId);

            // 1. 创建免费订阅套餐
            cn.iocoder.yudao.module.chrome.controller.admin.subscription.vo.SubscriptionSaveReqVO subscriptionReqVO = new cn.iocoder.yudao.module.chrome.controller.admin.subscription.vo.SubscriptionSaveReqVO();
            subscriptionReqVO.setUserId(userId);
            subscriptionReqVO.setSubscriptionType(SubscriptionTypeEnum.FREE.getCode());
            subscriptionReqVO.setStatus(true); // 激活状态
            subscriptionReqVO.setStartTime(LocalDateTime.now());
            // 免费套餐设置为永久有效（或者设置一个很长的时间）
            subscriptionReqVO.setEndTime(LocalDateTime.now().plusYears(100));
            subscriptionReqVO.setPaymentDuration(36500); // 100年
            subscriptionReqVO.setAutoRenew(false);

            subscriptionService.createSubscription(subscriptionReqVO);
            log.info("[createFreeSubscriptionAndCredits][用户({})免费套餐创建成功]", userId);

            // 2. 初始化用户积分账户（10积分）
            userCreditsService.initUserCredits(userId);
            log.info("[createFreeSubscriptionAndCredits][用户({})初始积分账户创建成功，获得10积分]", userId);

        } catch (Exception e) {
            log.error("[createFreeSubscriptionAndCredits][用户({})创建免费套餐和积分失败]", userId, e);
            // 这里不抛出异常，避免影响注册流程
        }
    }

    private void handleReferralBinding(Long userId, String referralCode) {
        try {
            UserDO referrer = userService.getUserByReferralCode(referralCode);
            if (referrer != null) {
                // 绑定关系
                UserDO updateObj = new UserDO();
                updateObj.setId(userId);
                updateObj.setReferrerUserId(referrer.getId());
                userService.updateUser(updateObj);
                log.info("[handleReferralBinding][用户({})成功绑定推荐人({})]", userId, referrer.getId());
            } else {
                log.warn("[handleReferralBinding][用户({})填写的推广码({})无效]", userId, referralCode);
            }
        } catch (Exception e) {
            log.error("[handleReferralBinding][用户({})绑定推荐关系失败]", userId, e);
        }
    }

    @Override
    public void removeUserTokens(Long userId) {
        try {
            oauth2TokenApi.removeAccessTokenByUserId(userId, UserTypeEnum.PLUGIN.getValue());
            // 删除用户的所有OAuth2访问令牌
            log.info("[removeUserTokens][剔除用户登录态成功，用户ID: {}]", userId);
        } catch (Exception e) {
            log.error("[removeUserTokens][剔除用户登录态失败，用户ID: {}]", userId, e);
            // 这里不抛出异常，避免影响删除用户流程
        }
    }
}