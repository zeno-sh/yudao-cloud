package cn.iocoder.yudao.module.bpm.api.event;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * 采购计划审批结果的监听器实现类
 * 
 * 用于在微服务模式下，通过 HTTP 调用 dm-server 更新审批状态
 *
 * @author Zeno
 */
@RequiredArgsConstructor
public class DmPurchasePlanStatusListener extends BpmProcessInstanceStatusEventListener {

    private static final String PROCESS_KEY = "dm_bpm_purchase_plan_flow";
    private static final String CONFIG_KEY = "dm_bpm_purchase_plan_flow";

    private final ConfigApi configApi;

    @Override
    public String getProcessDefinitionKey() {
        String configByKey = configApi.getConfigValueByKey(CONFIG_KEY).getCheckedData();
        return StrUtil.isBlank(configByKey) ? PROCESS_KEY : configByKey;
    }

    @Override
    public void onEvent(@RequestBody @Valid BpmProcessInstanceStatusEvent event) {
        BpmHttpRequestUtils.executeBpmHttpRequest(event,
                "http://dm-server/rpc-api/dm/purchase-plan/update-audit-status");
    }

}
