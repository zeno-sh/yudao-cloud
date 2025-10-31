package cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.consumer.order;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.NotifyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author zeno
 * @Date 2024/2/11
 */
@Component
public class NotificationProcessor {

    @Autowired
    private NotificationServiceFactory notificationServiceFactory;

    public String processNotification(NotifyRequest request) {
        String type = request.getMessage_type();
        NotificationService service = notificationServiceFactory.getService(type);
        if (service != null) {
            return service.process(request);
        } else {
            throw new RuntimeException("not found service listener");
        }
    }

}
