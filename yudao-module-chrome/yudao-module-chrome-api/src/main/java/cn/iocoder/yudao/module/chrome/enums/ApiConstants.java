package cn.iocoder.yudao.module.chrome.enums;

import cn.iocoder.yudao.framework.common.enums.RpcConstants;

/**
 * @Author: Jax
 * @Date: Created in 00:15 2026/1/19
 */
public class ApiConstants {
    /**
     * 服务名
     *
     * 注意，需要保证和 spring.application.name 保持一致
     */
    public static final String NAME = "chrome-server";

    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/chrome";

    public static final String VERSION = "1.0.0";
}
