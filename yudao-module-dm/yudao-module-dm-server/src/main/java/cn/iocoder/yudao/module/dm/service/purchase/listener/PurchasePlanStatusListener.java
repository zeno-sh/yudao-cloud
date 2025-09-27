package cn.iocoder.yudao.module.dm.service.purchase.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.dm.service.purchase.PurchasePlanService;
import cn.iocoder.yudao.module.dm.service.purchase.PurchasePlanServiceImpl;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: Zeno
 * @createTime: 2024/04/26 16:01
 */
@Component
@Slf4j
public class PurchasePlanStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private PurchasePlanService purchasePlanService;
    @Resource
    private ConfigApi configApi;

    @Override
    protected String getProcessDefinitionKey() {
        String configByKey = configApi.getConfigValueByKey("dm_bpm_purchase_plan_flow").getCheckedData();
        String processKey = StringUtils.isBlank(configByKey) ? PurchasePlanServiceImpl.PROCESS_KEY : configByKey;

        return processKey;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        log.info("审批流程监听：businessKey={}, status={}", event.getBusinessKey(), event.getStatus());
        purchasePlanService.updatePurchasePlanAuditStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }
}
