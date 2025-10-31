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
public class StateChangedListener extends BaseListener implements NotificationService {


    //    {
//            "message_type": "TYPE_STATE_CHANGED",
//            "posting_number": "24219509-0020-2",
//            "new_state": "posting_delivered",
//            "changed_state_date": "2021-02-02T15:07:46.765Z",
//            "warehouse_id": 0,
//            "seller_id": 15
//    }
    @Override
    public String process(NotifyRequest request) {
        String postingNumber = request.getPosting_number();
        String clientId = request.getSeller_id();

        OzonShopMappingDO dmShopMapping = queryByClientId(clientId);
        doFbs(dmShopMapping, postingNumber);
//        doFbo(dmShopMapping, postingNumber);

        Map<String, Boolean> map = new HashMap<>();
        map.put("result", true);
        return JSON.toJSONString(map);
    }

    @Override
    public NotifyTypeEnums getNotifyType() {
        return NotifyTypeEnums.TYPE_STATE_CHANGED;
    }

}
