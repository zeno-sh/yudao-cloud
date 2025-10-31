package cn.iocoder.yudao.module.dm.service.finance.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.dm.service.finance.FinancePaymentService;
import cn.iocoder.yudao.module.dm.service.finance.FinancePaymentServiceImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: Zeno
 * @createTime: 2024/05/17 15:47
 */
@Component
public class FinancePaymentListener extends BpmProcessInstanceStatusEventListener {
    @Resource
    private FinancePaymentService financePaymentService;

    @Override
    protected String getProcessDefinitionKey() {
        return FinancePaymentServiceImpl.PROCESS_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        financePaymentService.updateAuditStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }
}
