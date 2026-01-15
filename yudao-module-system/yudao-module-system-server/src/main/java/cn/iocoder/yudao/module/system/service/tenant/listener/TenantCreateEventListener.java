package cn.iocoder.yudao.module.system.service.tenant.listener;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.currency.CurrencyDO;
import cn.iocoder.yudao.module.system.framework.rpc.util.SystemHttpRequestUtils;
import cn.iocoder.yudao.module.system.service.currency.CurrencyService;
import cn.iocoder.yudao.module.system.service.tenant.event.TenantCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户创建事件监听器
 *
 * 监听新租户创建事件，通过 HTTP 调用 dm 模块初始化该租户的汇率数据
 * 
 * 注意：使用 HTTP 直接调用而非 Feign RPC，是为了避免 system-server 依赖 dm-api，
 * 因为 system 是基础服务，不应该依赖业务模块 dm。
 *
 * @author Zeno
 */
@Component
@Slf4j
public class TenantCreateEventListener {

    /**
     * dm-server 汇率初始化接口地址
     */
    private static final String DM_EXCHANGE_RATE_INIT_URL = "http://dm-server/rpc-api/dm/exchange/batch-init";

    @Resource
    private CurrencyService currencyService;

    /**
     * 处理租户创建事件
     *
     * @param event 租户创建事件
     */
    @EventListener
    @Async // 异步处理，避免阻塞租户创建主流程
    public void onTenantCreate(TenantCreateEvent event) {
        Long tenantId = event.getTenantId();
        log.info("[onTenantCreate] 接收到租户创建事件: tenantId={}", tenantId);

        try {
            // 1. 查询所有启用状态的币种
            List<String> currencyCodes = currencyService.getEnabledCurrencyList()
                    .stream()
                    .map(CurrencyDO::getCurrencyCode)
                    .collect(Collectors.toList());

            if (CollUtil.isEmpty(currencyCodes)) {
                log.warn("[onTenantCreate] 未找到启用的币种，跳过汇率初始化");
                return;
            }

            log.info("[onTenantCreate] 准备为租户({}) 初始化 {} 个币种的汇率", tenantId, currencyCodes.size());

            // 2. 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tenantId", tenantId);
            requestBody.put("currencyCodes", currencyCodes);

            // 3. 通过 HTTP 调用 dm 模块接口初始化汇率（租户 ID 会自动从上下文获取）
            SystemHttpRequestUtils.executeHttpRequest(requestBody, DM_EXCHANGE_RATE_INIT_URL);

            log.info("[onTenantCreate] 租户({}) 汇率初始化请求已发送", tenantId);

        } catch (Exception e) {
            log.error("[onTenantCreate] 租户({}) 汇率初始化异常", tenantId, e);
        }
    }

}
