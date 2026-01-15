package cn.iocoder.yudao.module.chrome.service.auth;

import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthLoginReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthLoginRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthRegisterReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthRegisterRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthResetPasswordReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo.ChromeAuthUpdatePasswordReqVO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Chrome认证 Service 接口
 *
 * @author Jax
 */
public interface ChromeAuthService {

    /**
     * 验证邮箱和密码 通过返回用户信息
     * @param email
     * @param password
     * @return
     */
    UserDO authenticate(String email, String password);

    /**
     * 邮箱密码登录
     *
     * @param reqVO 登录信息
     * @param clientIP 客户端IP
     * @param userAgent 用户代理
     * @return 登录结果
     */
    ChromeAuthLoginRespVO login(@Valid ChromeAuthLoginReqVO reqVO, String clientIP, String userAgent);

    /**
     * 登出
     *
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌信息
     */
    ChromeAuthLoginRespVO refreshToken(String refreshToken);

    /**
     * 获取登录用户的权限信息
     *
     * @return 用户权限信息
     */
    ChromeAuthLoginRespVO getPermissionInfo();

    /**
     * 从请求中获取访问令牌
     *
     * @param request HTTP请求
     * @return 访问令牌
     */
    String getAccessToken(HttpServletRequest request);

    /**
     * 创建访问令牌
     *
     * @param userId 用户ID
     * @param clientIP 客户端IP
     * @param userAgent 用户代理
     * @return 令牌信息
     */
    ChromeAuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String clientIP, String userAgent);

    /**
     * 用户注册
     *
     * @param reqVO 注册信息
     * @param clientIP 客户端 IP
     * @param userAgent 用户代理
     * @return 注册结果
     */
    ChromeAuthRegisterRespVO register(ChromeAuthRegisterReqVO reqVO, String clientIP, String userAgent);

    /**
     * 重置密码
     *
     * @param reqVO 重置密码信息
     * @param clientIP 客户端 IP
     * @param userAgent 用户代理
     */
    void resetPassword(ChromeAuthResetPasswordReqVO reqVO, String clientIP, String userAgent);

    /**
     * 修改密码
     *
     * @param reqVO 修改密码信息
     * @param clientIP 客户端 IP
     * @param userAgent 用户代理
     */
    void updatePassword(ChromeAuthUpdatePasswordReqVO reqVO, String clientIP, String userAgent);

    /**
     * 判断密码是否匹配
     *
     * @param rawPassword     未加密的密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean isPasswordMatch(String rawPassword, String encodedPassword);

    /**
     * 剔除用户的所有登录态
     *
     * @param userId 用户ID
     */
    void removeUserTokens(Long userId);
}