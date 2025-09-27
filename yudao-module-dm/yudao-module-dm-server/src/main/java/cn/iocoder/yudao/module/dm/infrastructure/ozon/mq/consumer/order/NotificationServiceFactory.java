package cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.consumer.order;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.NotifyTypeEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author zeno
 * @Date 2024/2/14
 */
@Component
public class NotificationServiceFactory {
    private final Map<NotifyTypeEnums, NotificationService> serviceMap = new HashMap<>();

    @Autowired
    public NotificationServiceFactory(List<NotificationService> services) {
        for (NotificationService service : services) {
            serviceMap.put(service.getNotifyType(), service);
        }
    }

    public NotificationService getService(String type) {
        NotifyTypeEnums notifyType = NotifyTypeEnums.fromType(type);
        return serviceMap.get(notifyType);
    }
}
