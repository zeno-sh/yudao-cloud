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
public class NewPostingListener extends BaseListener implements NotificationService {


    //    {
//        "message_type": "TYPE_NEW_POSTING",
//            "posting_number": "24219509-0020-1",
//            "products": [
//        {
//            "sku": 147451959,
//                "quantity": 2
//        }
//  ],
//        "in_process_at": "2021-01-26T06:56:36.294Z",
//            "warehouse_id": 18850503335000,
//            "seller_id": 15
//    }
    @Override
    public String process(NotifyRequest request) {

        String postingNumber = request.getPosting_number();
        String clientId = request.getSeller_id();
        OzonShopMappingDO dmShopMapping = queryByClientId(clientId);

//        doFbo(dmShopMapping, postingNumber);
        doFbs(dmShopMapping, postingNumber);

        Map<String, Boolean> map = new HashMap<>();
        map.put("result", true);
        return JSON.toJSONString(map);
    }

    @Override
    public NotifyTypeEnums getNotifyType() {
        return NotifyTypeEnums.TYPE_NEW_POSTING;
    }

}
