package cn.iocoder.yudao.module.dm.service.purchase.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.dm.enums.ApiConstants;
import cn.iocoder.yudao.module.dm.service.purchase.PurchasePlanService;
import cn.iocoder.yudao.module.dm.service.purchase.PurchasePlanServiceImpl;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 采购计划审批状态监听器
 * 
 * 注意：微服务模式下，BPM 事件通过 HTTP 调用此接口
 * 
 * @author: Zeno
 * @createTime: 2024/04/26 16:01
 */
@RestController
@Validated
@FeignClient(name = ApiConstants.NAME)
@Slf4j
public class PurchasePlanStatusListener extends BpmProcessInstanceStatusEventListener {

    private static final String PREFIX = ApiConstants.PREFIX + "/purchase-plan";

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
    @PostMapping(PREFIX + "/update-audit-status") // 提供给 bpm-server rpc 调用
    protected void onEvent(@RequestBody BpmProcessInstanceStatusEvent event) {
        log.info("审批流程监听：businessKey={}, status={}", event.getBusinessKey(), event.getStatus());
        purchasePlanService.updatePurchasePlanAuditStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }
}
