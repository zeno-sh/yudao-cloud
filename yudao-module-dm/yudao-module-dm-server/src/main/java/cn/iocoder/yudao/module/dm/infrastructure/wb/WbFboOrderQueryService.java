package cn.iocoder.yudao.module.dm.infrastructure.wb;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.service.OrderQueryService;
import cn.iocoder.yudao.module.dm.infrastructure.wb.constant.WbConfig;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.AllOrder;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Order;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbFboOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response.WbFboOrderQueryResponse;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response.WbOrderQueryResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Zeno
 * @createTime: 2024/08/07 16:50
 */
@Service
@Slf4j
public class WbFboOrderQueryService implements OrderQueryService<WbFboOrderQueryResponse, WbFboOrderQueryRequest> {

    @Resource
    private OrderQueryService<WbOrderQueryResponse, WbOrderQueryRequest> wbOrderQueryService;

    /**
     * 在个人账户中，无法查看FBO订单列表。
     * 使用API方法获取订单报告。按照条件 srid <> rid（srid 不等于 rid）对列表进行排序，这样您就可以获得FBO订单的列表。
     * 使用以下方法：https://marketplace-api.wildberries.ru/api/v3/orders 可以获取订单的rid
     * 使用以下方法：https://statistics-api.wildberries.ru/api/v1/supplier/orders  可以获取订单的srid。然后根据上述条件进行比较。
     * <p>
     * 使用说明：
     * https://openapi.wildberries.ru/marketplace/api/ru/#tag/Sborochnye-zadaniya/paths/~1api~1v3~1orders/get
     * https://openapi.wildberries.ru/statistics/api/ru/#tag/Statistika/paths/~1api~1v1~1supplier~1orders/get
     *
     * @param orderQueryRequest
     * @return
     */

    @Override
    public WbFboOrderQueryResponse queryOrder(WbFboOrderQueryRequest orderQueryRequest) {
        List<AllOrder> allOrderList = getAllOrderList(orderQueryRequest);
        List<Order> fbsOrderList = orderQueryRequest.getFbsOrderList();

        if (CollectionUtils.isEmpty(allOrderList)) {
            return null;
        }

        // API 更新：2025-05-09 11:56:09 使用 warehouseType = "Склад WB" 判断FBO订单
        List<AllOrder> filteredOrders = allOrderList.stream()
                .filter(allOrder -> "Склад WB".equals(allOrder.getWarehouseType()))
                .collect(Collectors.toList());

        List<Order> fboOrderList = new ArrayList<>();
        filteredOrders.forEach(allOrder -> {
            Order fboOrder = new Order();
            fboOrder.setId(Long.valueOf(allOrder.getGNumber().substring(0, 10)));
            fboOrder.setOrderUid(allOrder.getGNumber());
            fboOrder.setRid(allOrder.getSrid());
            fboOrder.setPrice(allOrder.getFinishedPrice() * 100);
            fboOrder.setConvertedPrice(allOrder.getTotalPrice() * 100);
            // 这里接收后时间会被转换成服务器的时区，即北京时间，所以需要+5处理
            fboOrder.setCreatedAt(DateUtil.offsetHour(allOrder.getDate(), 5));
            fboOrder.setSkus(Lists.newArrayList(allOrder.getBarcode()));
            fboOrder.setArticle(allOrder.getSupplierArticle());
            fboOrder.setNmId(allOrder.getNmId());

            if (allOrder.getIsCancel()) {
                fboOrder.setCanceled(Boolean.TRUE);
            }
            fboOrderList.add(fboOrder);
        });

        WbFboOrderQueryResponse response = new WbFboOrderQueryResponse();
        response.setOrders(fboOrderList);
        return response;
    }

    private List<AllOrder> getAllOrderList(WbFboOrderQueryRequest orderQueryRequest) {
        Map<String, Object> params = new HashMap<>();
        if (null != orderQueryRequest.getDateFrom()) {
            params.put("dateFrom", orderQueryRequest.getDateFrom());
//            params.put("dateTo", orderQueryRequest.getDateTo());
        }
        params.put("flag", orderQueryRequest.getFlag());

        TypeReference<List<AllOrder>> typeReference = new TypeReference<List<AllOrder>>() {
        };
        return get(orderQueryRequest.getToken(), WbConfig.WB_All_PRODUCT_LIST_API, params, typeReference);
    }

    private List<AllOrder> get(String token, String url, Map<String, Object> params, TypeReference<List<AllOrder>> typeReference) {
        try {
            log.info("查询 WB 接口，url = {}, request = {}", url, JSON.toJSONString(params));
            HttpResponse httpResponse = HttpRequest.get(url)
                    .header("accept", "application/json")
                    .header("Authorization", token)
                    .form(params)
                    .execute();


            if (httpResponse.getStatus() != 200) {
                log.error("查询 WB 接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
                return null;
            }
            String body = httpResponse.body();
            log.info("查询 WB 接口成功，url = {}, body = {}", url, body);

            return JSON.parseObject(body, typeReference);
        } catch (Exception e) {
            log.error("查询 WB 接口异常", e);
        }
        return null;
    }
}
