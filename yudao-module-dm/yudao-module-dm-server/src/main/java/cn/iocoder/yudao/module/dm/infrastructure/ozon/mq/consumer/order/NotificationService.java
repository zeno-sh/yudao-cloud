package cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.consumer.order;


import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.NotifyTypeEnums;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.NotifyRequest;

/**
 * @Author zeno
 * @Date 2024/2/11
 */
public interface NotificationService {

    String process(NotifyRequest request);

    NotifyTypeEnums getNotifyType();
}
