package cn.iocoder.yudao.module.data.framework.config;

import cn.iocoder.yudao.module.dm.api.DmShopMappingQueryService;
import cn.iocoder.yudao.module.sellfox.api.statistics.AmazonShopStatisticsApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * RPC 配置类
 * <p>
 * 启用各平台的 Feign 客户端
 * </p>
 *
 * @author Jax
 */
@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = {
        AmazonShopStatisticsApi.class,
        DmShopMappingQueryService.class
        // TODO: 后续添加其他平台
        // CoupangShopStatisticsApi.class,
})
public class RpcConfiguration {
}
