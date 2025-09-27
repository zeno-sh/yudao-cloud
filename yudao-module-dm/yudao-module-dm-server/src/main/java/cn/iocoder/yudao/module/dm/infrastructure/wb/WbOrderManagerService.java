package cn.iocoder.yudao.module.dm.infrastructure.wb;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.order.vo.OzonOrderSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import cn.iocoder.yudao.module.dm.infrastructure.service.OrderQueryService;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Order;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbFboOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response.WbFboOrderQueryResponse;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response.WbOrderQueryResponse;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Zeno
 * @createTime: 2024/07/12 23:26
 */
@Service
@Slf4j
public class WbOrderManagerService {

    @Resource
    private OzonOrderService dmOrderService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private OrderQueryService<WbOrderQueryResponse, WbOrderQueryRequest> wbOrderQueryService;
    @Resource
    private OrderQueryService<WbFboOrderQueryResponse, WbFboOrderQueryRequest> wbFboOrderQueryService;

    public void syncOrder(String clientId, String utcBeginDateTime, String utcEndDateTime) {

        log.info("开始同步订单");
        OzonShopMappingDO dmShopMapping = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        if (null == dmShopMapping) {
            log.error("没有找到店铺信息");
            return;
        }
        doFbs(dmShopMapping, utcBeginDateTime, utcEndDateTime);
    }

    /**
     * 获取FBS订单
     *
     * @param dmShopMapping
     * @param beginDate     yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss 格式
     * @param endDate       yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss 格式
     */
    private void doFbs(OzonShopMappingDO dmShopMapping, String beginDate, String endDate) {
        WbOrderQueryRequest request = new WbOrderQueryRequest();
        request.setClientId(dmShopMapping.getClientId());
        request.setToken(dmShopMapping.getApiKey());
        request.setLimit(500);
        request.setNext(0L);

        // 按照WildBerries API要求，直接设置Unix时间戳
        try {
            // 移除T和Z，格式化为标准日期时间格式
            String beginDateTime = beginDate.replace("T", " ").replace("Z", "");
            String endDateTime = endDate.replace("T", " ").replace("Z", "");
            
            // 设置开始日期和结束日期对应的时间戳
            if (beginDateTime.contains(" ")) {
                // 如果包含时间部分，直接转换为时间戳
                request.setDateFrom(convertDateTimeToTimestamp(beginDateTime));
            } else {
                // 如果只有日期部分，设置为当天开始(0点)
                request.setDateFrom(convertDateToStartOfDayTimestamp(beginDateTime));
            }
            
            if (endDateTime.contains(" ")) {
                // 如果包含时间部分，直接转换为时间戳
                request.setDateTo(convertDateTimeToTimestamp(endDateTime));
            } else {
                // 如果只有日期部分，设置为当天结束(23:59:59)
                request.setDateTo(convertDateToEndOfDayTimestamp(endDateTime));
            }
        } catch (Exception e) {
            log.error("日期转换错误: " + e.getMessage(), e);
            // 默认使用最近30天
            long now = System.currentTimeMillis() / 1000;
            request.setDateFrom(now - 30 * 24 * 60 * 60);
            request.setDateTo(now);
        }

        // 因为WB接口的限制，这里会复用fbs的订单结果
        List<Order> orders = saveOrder(dmShopMapping.getTenantId(), request);
        doFbo(dmShopMapping, beginDate, endDate, orders);
    }

    /**
     * 将日期字符串转换为当天开始(0点)的Unix时间戳
     */
    private long convertDateToStartOfDayTimestamp(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return date.atStartOfDay().atZone(DmDateUtils.MOSCOW_ZONE_ID)
                .withZoneSameInstant(DmDateUtils.UTC_ZONE_ID)
                .toInstant().getEpochSecond();
    }

    /**
     * 将日期字符串转换为当天结束(23:59:59)的Unix时间戳
     */
    private long convertDateToEndOfDayTimestamp(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return date.atTime(LocalTime.MAX).atZone(DmDateUtils.MOSCOW_ZONE_ID)
                .withZoneSameInstant(DmDateUtils.UTC_ZONE_ID)
                .toInstant().getEpochSecond();
    }

    /**
     * 将日期时间字符串转换为Unix时间戳
     */
    private long convertDateTimeToTimestamp(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        return dateTime.atZone(DmDateUtils.MOSCOW_ZONE_ID)
                .withZoneSameInstant(DmDateUtils.UTC_ZONE_ID)
                .toInstant().getEpochSecond();
    }

    private void doFbo(OzonShopMappingDO dmShopMapping, String beginDate, String endDate, List<Order> fbsOrders) {
        WbFboOrderQueryRequest request = new WbFboOrderQueryRequest();
        request.setClientId(dmShopMapping.getClientId());
        request.setToken(dmShopMapping.getApiKey());
        request.setFlag(1);
//        request.setFbsOrderList(fbsOrders);

        request.setDateFrom(beginDate);
        request.setDateTo(endDate);
        WbFboOrderQueryResponse response = wbFboOrderQueryService.queryOrder(request);
        if (response != null && CollectionUtils.isNotEmpty(response.getOrders())) {
            insertOrders(dmShopMapping.getTenantId(), dmShopMapping.getClientId(), response.getOrders(), 30);
        }
    }

    public List<Order> saveOrder(Long tenantId, WbOrderQueryRequest reqDTO) {
        Boolean hasNextPage = true;
        List<Order> orders = new ArrayList<>();
        while (hasNextPage) {
            WbOrderQueryResponse response = wbOrderQueryService.queryOrder(reqDTO);
            if (response != null) {
                if (CollectionUtils.isNotEmpty(response.getOrders())) {
                    orders.addAll(response.getOrders());
                    insertOrders(tenantId, reqDTO.getClientId(), response.getOrders(), 20);
                } else {
                    hasNextPage = false;
                }

                if (hasNextPage) {
                    reqDTO.setNext(response.getNext());
                }
            } else {
                hasNextPage = false;
            }
        }
        return orders;
    }

    private void insertOrders(Long tenantId, String clientId, List<Order> orders, Integer orderType) {
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }
        for (Order order : orders) {
            OzonOrderDO dmOrderDO = new OzonOrderDO();
            dmOrderDO.setTenantId(tenantId);
            dmOrderDO.setClientId(clientId);
            dmOrderDO.setOrderId(String.valueOf(order.getId()));
            dmOrderDO.setOrderNumber(order.getOrderUid());
            dmOrderDO.setPostingNumber(order.getRid());
            dmOrderDO.setProducts(JSON.toJSONString(order.getSkus()));
            if (order.getCanceled() != null && order.getCanceled()) {
                dmOrderDO.setStatus("canceled");
                dmOrderDO.setSubstatus("posting_canceled");
            } else {
                dmOrderDO.setStatus("waiting");
                dmOrderDO.setSubstatus("new");
            }

            // wb这里价格是分为单位
            dmOrderDO.setAccrualsForSale(new BigDecimal(order.getConvertedPrice()).divide(new BigDecimal("100")));
            dmOrderDO.setOrderType(orderType);
            if (order.getCreatedAt() != null) {
                dmOrderDO.setInProcessAt(LocalDateTimeUtil.ofUTC(order.getCreatedAt().toInstant()));
            }

            List<OzonOrderDO> existOrderList;

            // wb fbo 订单
            if (orderType == 30) {
                existOrderList = dmOrderService.getOzonOrderListByPlatformOrderId(clientId, String.valueOf(order.getId()), order.getRid(), null, null);
            } else {
                existOrderList = dmOrderService.getOzonOrderListByPlatformOrderId(clientId, String.valueOf(order.getId()), null, null, null);
                Map<String, String> warehouseMap = new HashMap<>();
                warehouseMap.put("warehouse_id", String.valueOf(order.getWarehouseId()));
                dmOrderDO.setDeliveryMethod(JSON.toJSONString(warehouseMap));
            }

            if (CollectionUtils.isEmpty(existOrderList)) {
                dmOrderService.createOzonOrder(BeanUtils.toBean(dmOrderDO, OzonOrderSaveReqVO.class));
            } else {
                dmOrderDO.setId(existOrderList.get(0).getId());
                //再次重新复制，防止java处理成本地时区
                dmOrderDO.setInProcessAt(LocalDateTimeUtil.ofUTC(order.getCreatedAt().toInstant()));
                dmOrderService.updateOzonOrder(BeanUtils.toBean(dmOrderDO, OzonOrderSaveReqVO.class));
            }

            //保存订单商品明细
            saveOrderItem(tenantId, clientId, order);
        }
    }

    private void saveOrderItem(Long tenantId, String clientId, Order order) {
        OzonOrderItemDO orderItem = new OzonOrderItemDO();
        orderItem.setTenantId(tenantId);
        orderItem.setClientId(clientId);
        orderItem.setOrderId(String.valueOf(order.getId()));
        orderItem.setPostingNumber(order.getRid());
        orderItem.setPlatformSkuId(String.valueOf(order.getNmId()));
        orderItem.setOfferId(order.getArticle());
        orderItem.setQuantity(1);
        orderItem.setPrice(new BigDecimal(order.getConvertedPrice()).divide(new BigDecimal("100")));
        orderItem.setInProcessAt(LocalDateTimeUtil.ofUTC(order.getCreatedAt().toInstant()));

        List<OzonOrderItemDO> dmOrderItems = dmOrderService.getOrderItemListByOrderIdAndPostingNumber(clientId, String.valueOf(order.getId()), order.getRid());
        if (CollectionUtils.isEmpty(dmOrderItems)) {
            dmOrderService.createOzonOrderItemList(String.valueOf(order.getId()), Lists.newArrayList(orderItem));
        } else {
            orderItem.setId(dmOrderItems.get(0).getId());
            orderItem.setInProcessAt(LocalDateTimeUtil.ofUTC(order.getCreatedAt().toInstant()));
            dmOrderService.updateOzonOrderItemList(String.valueOf(order.getId()), Lists.newArrayList(orderItem));
        }
    }

}
