package cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.producer.ad;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.message.ad.AdMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 广告数据同步任务
 * @author: Zeno
 * @createTime: 2024/11/26 21:07
 */
@Component
@Slf4j
public class AdProducer {

    @Resource
    private ApplicationContext applicationContext;

    public void sendAdMessage(Long tenantId, String clientId, String beginDate, String endDate) {
        AdMessage adMessage = new AdMessage();
        adMessage.setTenantId(tenantId);
        adMessage.setClientId(clientId);
        adMessage.setBeginDate(beginDate);
        adMessage.setEndDate(endDate);
        applicationContext.publishEvent(adMessage);
    }
}
