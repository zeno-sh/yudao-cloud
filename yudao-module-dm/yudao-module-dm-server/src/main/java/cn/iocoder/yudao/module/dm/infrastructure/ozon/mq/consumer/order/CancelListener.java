package cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.consumer.order;

import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.NotifyTypeEnums;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.NotifyRequest;
import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author zeno
 * @Date 2024/2/11
 */
@Component
public class CancelListener extends BaseListener implements NotificationService {

    @Override
    public String process(NotifyRequest request) {
        String postingNumber = request.getPosting_number();
        String clientId = request.getSeller_id();
        OzonShopMappingDO dmShopMapping = queryByClientId(clientId);
        doFbs(dmShopMapping, postingNumber);

        Map<String, Boolean> map = new HashMap<>();
        map.put("result", true);
        return JSON.toJSONString(map);
    }

    @Override
    public NotifyTypeEnums getNotifyType() {
        return NotifyTypeEnums.TYPE_POSTING_CANCELLED;
    }
}
