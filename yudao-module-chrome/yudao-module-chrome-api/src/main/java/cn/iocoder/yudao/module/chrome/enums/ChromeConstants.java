package cn.iocoder.yudao.module.chrome.enums;

/**
 * Chrome模块常量
 *
 * @author Jax
 */
public interface ChromeConstants {
    
    /**
     * 邮箱验证码相关常量
     */
    interface EmailCode {
        /** 验证码过期时间（分钟） */
        int EXPIRE_MINUTES = 5;
        /** 验证码长度 */
        int CODE_LENGTH = 6;
        /** 每日最大发送次数 */
        int MAX_SEND_COUNT_PER_DAY = 10;
        /** 发送间隔（秒） */
        int SEND_INTERVAL_SECONDS = 60;
    }
}