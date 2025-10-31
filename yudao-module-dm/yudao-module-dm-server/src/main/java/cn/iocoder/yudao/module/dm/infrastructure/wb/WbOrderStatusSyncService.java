package cn.iocoder.yudao.module.dm.infrastructure.wb;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.order.vo.OzonOrderSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import cn.iocoder.yudao.module.dm.infrastructure.wb.constant.WbConfig;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.OrderItem;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbOrderStatusRequest;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response.WbOrderStatusResponse;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response.WbProductResponse;
import cn.iocoder.yudao.module.dm.infrastructure.wb.utils.WbHttpUtils;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * @author: Zeno
 * @createTime: 2024/07/13 20:08
 */
@Service
public class WbOrderStatusSyncService {

    @Resource
    private WbHttpUtils wbHttpUtils;
    @Resource
    private OzonOrderService ozonOrderService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;

    /**
     * 同步订单状态
     *
     * @param clientId
     * @param beginDate yyyy-MM-dd
     * @param endDate
     */
    public void sync(String clientId, String beginDate, String endDate) {
        OzonShopMappingDO shopMappingDO = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        Integer platform = shopMappingDO.getPlatform();
        LocalDateTime[] localDateTimes = convertLocalDateTime(beginDate, endDate);

        if (DmPlatformEnum.WB_GLOBAL.getPlatformId().equals(platform) || DmPlatformEnum.WB.getPlatformId().equals(platform)) {
            List<OzonOrderDO> orderList = ozonOrderService.getOzonOrderList(shopMappingDO.getClientId(), null,
                    localDateTimes[0], localDateTimes[1]);
            if (CollectionUtils.isEmpty(orderList)) {
                return;
            }

            List<List<OzonOrderDO>> partition = Lists.partition(orderList, 1000);
            for (List<OzonOrderDO> part : partition) {
                List<Long> orderIds = convertList(part, orderDO -> Long.valueOf(orderDO.getOrderId()));
                WbOrderStatusResponse response = getOrderStatus(shopMappingDO, orderIds);
                if (null != response) {
                    updateOrderStatus(part, response.getOrders());
                }
            }
        }
    }

    private LocalDateTime[] convertLocalDateTime(String beginDate, String endDate) {
        // 时区转换 -> 莫斯科时区
        LocalDateTime[] inProcessAtMoscowLocalDateTimes = new LocalDateTime[2];
        String beginDateTime = DmDateUtils.formatStartOfDay(beginDate, DatePattern.NORM_DATETIME_PATTERN);
        String endDateTime = DmDateUtils.formatEndOfDay(endDate, DatePattern.NORM_DATETIME_PATTERN);
        inProcessAtMoscowLocalDateTimes[0] = LocalDateTimeUtil.of(DateUtil.parseDateTime(beginDateTime).toInstant());
        inProcessAtMoscowLocalDateTimes[1] = LocalDateTimeUtil.of(DateUtil.parseDateTime(endDateTime).toInstant());

        return inProcessAtMoscowLocalDateTimes;
    }

    public void syncByOrderId(OzonShopMappingDO wbShopMappingDO, List<String> orderIds) {
        List<OzonOrderDO> orderList = ozonOrderService.getOrderList(wbShopMappingDO.getClientId(), orderIds);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }

        List<List<OzonOrderDO>> partition = Lists.partition(orderList, 1000);
        for (List<OzonOrderDO> part : partition) {
            List<Long> orderIdList = convertList(part, orderDO -> Long.valueOf(orderDO.getOrderId()));
            WbOrderStatusResponse response = getOrderStatus(wbShopMappingDO, orderIdList);
            if (null != response) {
                updateOrderStatus(part, response.getOrders());
            }
        }
    }

    private void updateOrderStatus(List<OzonOrderDO> orderList, List<OrderItem> orderItems) {

        if (CollectionUtils.isEmpty(orderItems)) {
            return;
        }

        Map<String, OrderItem> orderItemMap = convertMap(orderItems, orderItem -> String.valueOf(orderItem.getId()));

        for (OzonOrderDO orderDO : orderList) {
            OrderItem orderItem = orderItemMap.get(orderDO.getOrderId());
            if (orderItem != null) {
                orderDO.setStatus(orderItem.getWbStatus());
                orderDO.setSubstatus(orderItem.getSupplierStatus());
                ozonOrderService.updateOzonOrder(BeanUtils.toBean(orderDO, OzonOrderSaveReqVO.class));
            }
        }


    }

    private WbOrderStatusResponse getOrderStatus(OzonShopMappingDO wbShopMappingDO, List<Long> orderIds) {

        WbOrderStatusRequest request = new WbOrderStatusRequest();
        request.setOrders(orderIds);
        request.setClientId(wbShopMappingDO.getClientId());
        request.setToken(wbShopMappingDO.getApiKey());

        TypeReference<WbOrderStatusResponse> typeReference = new TypeReference<WbOrderStatusResponse>() {
        };
        return wbHttpUtils.post(WbConfig.WB_ORDER_STATUS_API, request, typeReference);
    }
}
