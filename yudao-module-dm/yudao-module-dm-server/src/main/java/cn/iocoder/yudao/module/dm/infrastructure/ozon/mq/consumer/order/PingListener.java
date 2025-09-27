package cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.consumer.order;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.NotifyTypeEnums;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.OzonMessageSuccessDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.NotifyRequest;
import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

/**
 * @Author zeno
 * @Date 2024/2/11
 */
@Component
public class PingListener implements NotificationService {
    @Override
    public String process(NotifyRequest request) {
        OzonMessageSuccessDTO ozonMessageSuccess = new OzonMessageSuccessDTO();
        ozonMessageSuccess.setVersion("v1.0.0");

        DateTime utc = DateUtil.convertTimeZone(DateUtil.date(), TimeZone.getTimeZone("UTC"));
        ozonMessageSuccess.setTime(DateUtil.format(utc, DatePattern.UTC_PATTERN));
        ozonMessageSuccess.setName("zeno erp");

        return JSON.toJSONString(ozonMessageSuccess);
    }

    @Override
    public NotifyTypeEnums getNotifyType() {
        return NotifyTypeEnums.TYPE_PING;
    }
}
