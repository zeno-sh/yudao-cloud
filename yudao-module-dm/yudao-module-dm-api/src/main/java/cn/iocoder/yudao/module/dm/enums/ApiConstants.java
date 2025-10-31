package cn.iocoder.yudao.module.dm.enums;

import cn.iocoder.yudao.framework.common.enums.RpcConstants;

/**
 * @Author: Jax
 * @Date: Created in 11:45 2025/9/28
 */
public class ApiConstants {
    /**
     * 服务名
     *
     * 注意，需要保证和 spring.application.name 保持一致
     */
    public static final String NAME = "dm-server";

    public static final String PREFIX = RpcConstants.RPC_API_PREFIX +  "/dm";

    public static final String VERSION = "1.0.0";
}
