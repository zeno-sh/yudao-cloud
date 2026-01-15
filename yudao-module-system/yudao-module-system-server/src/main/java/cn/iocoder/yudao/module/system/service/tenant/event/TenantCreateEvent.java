package cn.iocoder.yudao.module.system.service.tenant.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 租户创建事件
 *
 * @author Zeno
 */
@Getter
public class TenantCreateEvent extends ApplicationEvent {

    /**
     * 租户编号
     */
    private final Long tenantId;

    public TenantCreateEvent(Object source, Long tenantId) {
        super(source);
        this.tenantId = tenantId;
    }

}
