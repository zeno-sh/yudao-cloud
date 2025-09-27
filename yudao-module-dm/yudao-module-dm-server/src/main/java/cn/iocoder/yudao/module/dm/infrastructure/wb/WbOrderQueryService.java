package cn.iocoder.yudao.module.dm.infrastructure.wb;

import cn.iocoder.yudao.module.dm.infrastructure.service.OrderQueryService;
import cn.iocoder.yudao.module.dm.infrastructure.wb.constant.WbConfig;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response.WbOrderQueryResponse;
import cn.iocoder.yudao.module.dm.infrastructure.wb.utils.WbHttpUtils;
import com.alibaba.fastjson2.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Zeno
 * @createTime: 2024/07/10 23:23
 */
@Service
public class WbOrderQueryService implements OrderQueryService<WbOrderQueryResponse, WbOrderQueryRequest> {

    @Resource
    private WbHttpUtils wbHttpUtils;

    @Override
    public WbOrderQueryResponse queryOrder(WbOrderQueryRequest orderQueryRequest) {
        Map<String, Object> params = new HashMap<>();
        if (null != orderQueryRequest.getDateFrom()) {
            params.put("dateFrom", orderQueryRequest.getDateFrom());
            params.put("dateTo", orderQueryRequest.getDateTo());
        }
        params.put("limit", orderQueryRequest.getLimit());
        params.put("next", orderQueryRequest.getNext());

        TypeReference<WbOrderQueryResponse> typeReference = new TypeReference<WbOrderQueryResponse>() {
        };
        return wbHttpUtils.get(orderQueryRequest.getToken(), WbConfig.WB_ORDER_LIST_API, params, typeReference);
    }

}
