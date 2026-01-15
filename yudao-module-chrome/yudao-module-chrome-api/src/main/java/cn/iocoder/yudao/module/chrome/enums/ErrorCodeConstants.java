package cn.iocoder.yudao.module.chrome.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Chrome 错误码枚举类
 *
 * chrome 系统，使用 1-030-000-000 段
 *
 * @author Jax
 */
public interface ErrorCodeConstants {

    // ========== 用户模块 1-030-001-000 ==========
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1_030_001_000, "用户不存在");
    ErrorCode USER_EMAIL_EXISTS = new ErrorCode(1_030_001_001, "邮箱已存在");
    ErrorCode USER_PASSWORD_ERROR = new ErrorCode(1_030_001_002, "密码错误");
    ErrorCode USER_DEVICE_TOKEN_EXISTS = new ErrorCode(1_030_001_003, "该设备已注册过账号，每台设备仅允许注册一个账号");
    ErrorCode USER_CAPTCHA_CODE_ERROR = new ErrorCode(1_030_001_004, "验证码不正确");
    ErrorCode USER_IS_DISABLE = new ErrorCode(1_030_001_005, "用户已被禁用");
    ErrorCode USER_PASSWORD_FAILED = new ErrorCode(1_030_001_006, "密码校验失败");
    ErrorCode PASSWORD_CONFIRM_NOT_MATCH = new ErrorCode(1_030_001_007, "两次输入的密码不一致");
    ErrorCode UNAUTHORIZED = new ErrorCode(1_030_001_008, "用户未登录或登录已过期");

    // ========== 订阅模块 1-030-002-000 ==========
    ErrorCode SUBSCRIPTION_NOT_EXISTS = new ErrorCode(1_030_002_000, "订阅不存在");
    ErrorCode SUBSCRIPTION_NOT_FOUND = new ErrorCode(1_030_002_001, "未找到订阅信息");
    ErrorCode SUBSCRIPTION_EXPIRED = new ErrorCode(1_030_002_002, "订阅已过期，请续费后使用");
    ErrorCode SUBSCRIPTION_NOT_ACTIVE = new ErrorCode(1_030_002_003, "订阅未激活");
    ErrorCode SUBSCRIPTION_DOWNGRADE_NOT_ALLOWED = new ErrorCode(1_030_002_004, "不允许降级订阅，只能升级或续费相同套餐");

    // ========== 使用记录模块 1-030-003-000 ==========
    ErrorCode USAGE_RECORD_NOT_EXISTS = new ErrorCode(1_030_003_000, "使用记录不存在");

    // ========== 积分模块 1-030-004-000 ==========
    ErrorCode CREDITS_INSUFFICIENT = new ErrorCode(1_030_004_000, "积分不足，无法使用该功能");

    // ========== 邮箱验证码模块 1-030-005-000 ==========
    ErrorCode EMAIL_CODE_NOT_EXISTS = new ErrorCode(1_030_005_000, "邮箱验证码不存在");
    ErrorCode EMAIL_CODE_INVALID = new ErrorCode(1_030_005_001, "邮箱验证码无效");
    ErrorCode EMAIL_CODE_EXPIRED = new ErrorCode(1_030_005_002, "邮箱验证码已过期");
    ErrorCode EMAIL_CODE_USED = new ErrorCode(1_030_005_003, "邮箱验证码已使用");
    ErrorCode EMAIL_CODE_DAILY_LIMIT_EXCEEDED = new ErrorCode(1_030_005_004, "邮箱验证码每日发送次数已达上限");
    ErrorCode EMAIL_CODE_SEND_TOO_FREQUENT = new ErrorCode(1_030_005_005, "邮箱验证码发送过于频繁");
    ErrorCode EMAIL_CODE_PURPOSE_MISMATCH = new ErrorCode(1_030_005_006, "邮箱验证码用途不匹配");
    ErrorCode EMAIL_CODE_SEND_FAILED = new ErrorCode(1_030_005_007, "邮箱验证码发送失败");

    // ========== 业务实体模块 1-030-006-000 ==========
    ErrorCode SUBSCRIPTION_PLAN_NOT_EXISTS = new ErrorCode(1_030_006_000, "订阅计划不存在");
    ErrorCode SUBSCRIPTION_PLAN_TYPE_ERROR = new ErrorCode(1_030_006_001, "订阅计划类型错误");
    ErrorCode SUBSCRIPTION_PLAN_DISABLED = new ErrorCode(1_030_006_002, "订阅计划已禁用");
    ErrorCode USER_CREDITS_NOT_EXISTS = new ErrorCode(1_030_006_003, "用户积分记录不存在");
    ErrorCode SUBSCRIPTION_ORDER_NOT_EXISTS = new ErrorCode(1_030_006_004, "订阅订单不存在");
    ErrorCode CREDITS_TRANSACTION_NOT_EXISTS = new ErrorCode(1_030_006_005, "积分交易记录不存在");
    ErrorCode CLIENT_SERVER_NOT_EXISTS = new ErrorCode(1_030_006_006, "客户端服务器不存在");

    // ========== 业务功能模块 1-030-007-000 ==========
    ErrorCode PRODUCT_REVIEW_QUERY_COOKIE_ERROR = new ErrorCode(1_030_007_000, "获取前台Cookie失败");
    ErrorCode PRODUCT_SALES_QUERY_COOKIE_ERROR = new ErrorCode(1_030_007_001, "获取后台Cookie失败");

}