package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderSupplyDO;
import cn.iocoder.yudao.module.dm.dal.mysql.ozonsupplyorder.OzonSupplyOrderItemMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.ozonsupplyorder.OzonSupplyOrderMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.ozonsupplyorder.OzonSupplyOrderSupplyMapper;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.HttpBaseRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.SupplyOrderBundleRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.SupplyOrderGetRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.SupplyOrderListRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.SupplyOrderBundleResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.SupplyOrderGetResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.SupplyOrderListResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * Ozon 供应订单管理 Service
 *
 * @author Zeno
 */
@Service
@Slf4j
public class SupplyOrderManagerService {

    private static final String SUPPLY_ORDER_LIST_URL = "https://api-seller.ozon.ru/v2/supply-order/list";
    private static final String SUPPLY_ORDER_GET_URL = "https://api-seller.ozon.ru/v2/supply-order/get";
    private static final String SUPPLY_ORDER_BUNDLE_URL = "https://api-seller.ozon.ru/v1/supply-order/bundle";

    private static final int MAX_WAIT_SECONDS = 1;
    private static final int MAX_LOOP_COUNT = 100;
    private static final int MAX_EXECUTION_MINUTES = 30;
    private static final int PAGE_SIZE = 100;
    private static final int MAX_ORDER_IDS_PER_REQUEST = 50;

    @Resource
    private OzonHttpUtil<HttpBaseRequest> ozonHttpUtil;
    @Resource
    private OzonSupplyOrderMapper ozonSupplyOrderMapper;
    @Resource
    private OzonSupplyOrderItemMapper ozonSupplyOrderItemMapper;
    @Resource
    private OzonSupplyOrderSupplyMapper ozonSupplyOrderSupplyMapper;
    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private OzonProductOnlineService ozonProductOnlineService;

    private final RateLimiter rateLimiter = RateLimiter.create(10.0);

    @Transactional(rollbackFor = Exception.class)
    public void syncSupplyOrders(String clientId, String apiKey) {
        // 1. 先从数据库获取所有订单信息
        List<OzonSupplyOrderDO> existingOrders = ozonSupplyOrderMapper.selectList(
                new LambdaQueryWrapperX<OzonSupplyOrderDO>()
                        .eq(OzonSupplyOrderDO::getClientId, clientId)
                        .eq(OzonSupplyOrderDO::getUpdatedManually, false));

        // 2. 获取需要更新的订单（未完成状态）
        List<String> needUpdateOrderIds = new ArrayList<>();
        if (CollUtil.isNotEmpty(existingOrders)) {
            for (OzonSupplyOrderDO order : existingOrders) {
                if (!"ORDER_STATE_COMPLETED".equals(order.getState())) {
                    needUpdateOrderIds.add(String.valueOf(order.getSupplyOrderId()));
                }
            }
        }

        // 3. 获取缺少关联数据的订单
        List<String> needSupplyDataOrderIds = new ArrayList<>();
        List<String> needItemDataOrderIds = new ArrayList<>();
        if (CollUtil.isNotEmpty(existingOrders)) {
            for (OzonSupplyOrderDO order : existingOrders) {
                String orderId = String.valueOf(order.getSupplyOrderId());

                // 检查是否有供应关系数据
                List<OzonSupplyOrderSupplyDO> supplies = ozonSupplyOrderSupplyMapper.selectList(
                        new LambdaQueryWrapperX<OzonSupplyOrderSupplyDO>()
                                .eq(OzonSupplyOrderSupplyDO::getClientId, clientId)
                                .eq(OzonSupplyOrderSupplyDO::getSupplyOrderId, order.getSupplyOrderId()));
                if (CollUtil.isEmpty(supplies)) {
                    needSupplyDataOrderIds.add(orderId);
                }

                // 检查是否有商品明细数据
                List<OzonSupplyOrderItemDO> items = ozonSupplyOrderItemMapper.selectList(
                        new LambdaQueryWrapperX<OzonSupplyOrderItemDO>()
                                .eq(OzonSupplyOrderItemDO::getClientId, clientId)
                                .eq(OzonSupplyOrderItemDO::getSupplyOrderId, order.getSupplyOrderId()));
                if (CollUtil.isEmpty(items)) {
                    needItemDataOrderIds.add(orderId);
                }
            }
        }

        // 4. 获取最新订单列表（可能有新订单）
        List<String> newOrderIds = new ArrayList<>();
        if (!acquireRateLimit("[syncSupplyOrderList]", clientId)) {
            return;
        }
        List<String> latestOrderIds = syncSupplyOrderList(clientId, apiKey);
        if (CollUtil.isNotEmpty(latestOrderIds)) {
            for (String orderId : latestOrderIds) {
                if (existingOrders.stream().noneMatch(order ->
                        String.valueOf(order.getSupplyOrderId()).equals(orderId))) {
                    newOrderIds.add(orderId);
                }
            }
        }

        // 5. 合并所有需要处理的订单ID
        Set<String> allNeedSyncOrderIds = new HashSet<>();
        allNeedSyncOrderIds.addAll(needUpdateOrderIds);
        allNeedSyncOrderIds.addAll(needSupplyDataOrderIds);
        allNeedSyncOrderIds.addAll(needItemDataOrderIds);
        allNeedSyncOrderIds.addAll(newOrderIds);

        if (CollUtil.isEmpty(allNeedSyncOrderIds)) {
            log.info("[syncSupplyOrders][无需同步的订单]clientId: {}", clientId);
            return;
        }

        // 6. 同步订单详情
        List<List<String>> orderIdBatches = CollUtil.split(new ArrayList<>(allNeedSyncOrderIds), MAX_ORDER_IDS_PER_REQUEST);
        List<SupplyOrderGetResponse.Order> allOrders = new ArrayList<>();
        for (List<String> batchOrderIds : orderIdBatches) {
            if (!acquireRateLimit("[syncSupplyOrderDetails]", clientId)) {
                continue;
            }

            SupplyOrderGetResponse response = getSupplyOrderDetails(clientId, apiKey, batchOrderIds);
            if (!validateResponse(response, "[syncSupplyOrderDetails]", clientId)) {
                continue;
            }

            // 保存订单详情和供应关系
            saveOrUpdateSupplyOrders(clientId, response);
            if (CollUtil.isNotEmpty(response.getOrders())) {
                allOrders.addAll(response.getOrders());
            }
        }

        // 7. 同步商品明细（仅对需要的订单）
        if (CollUtil.isNotEmpty(allOrders)) {
            for (SupplyOrderGetResponse.Order order : allOrders) {
                String orderId = order.getSupplyOrderId();
                // 只同步需要更新商品明细的订单
                if (!needItemDataOrderIds.contains(orderId) && !newOrderIds.contains(orderId)) {
                    continue;
                }
                syncAllSupplyOrderItems(clientId, apiKey, Collections.singletonList(order));
            }
        }
    }

    /**
     * 同步供应订单列表
     */
    private List<String> syncSupplyOrderList(String clientId, String apiKey) {
        Long fromSupplyOrderId = getLastSupplyOrderId(clientId);
        long startTime = System.currentTimeMillis();
        int loopCount = 0;
        Long previousFromSupplyOrderId = null;
        List<String> allOrderIds = CollUtil.newArrayList();

        while (true) {
            // 检查执行限制
            if (!checkExecutionLimits(startTime, ++loopCount, clientId)) {
                return allOrderIds;
            }
            // 检查重复ID
            if (Objects.equals(previousFromSupplyOrderId, fromSupplyOrderId)) {
                log.error("[syncSupplyOrderList][检测到重复的fromSupplyOrderId: {}]clientId: {}", fromSupplyOrderId, clientId);
                return allOrderIds;
            }
            previousFromSupplyOrderId = fromSupplyOrderId;

            // 获取订单列表
            if (!acquireRateLimit("[syncSupplyOrderList]", clientId)) {
                return allOrderIds;
            }
            SupplyOrderListResponse response = getSupplyOrderList(clientId, apiKey, fromSupplyOrderId);
            if (!validateResponse(response, "[syncSupplyOrderList]", clientId)) {
                return allOrderIds;
            }

            List<String> orderIds = response.getSupplyOrderIds();
            if (CollUtil.isEmpty(orderIds)) {
                return allOrderIds;
            }

            // 添加本次获取的订单ID
            allOrderIds.addAll(orderIds);
            log.info("[syncSupplyOrderList][本次获取订单数: {}，累计订单数: {}]clientId: {}",
                    orderIds.size(), allOrderIds.size(), clientId);

            // 检查是否还有更多数据
            Long lastSupplyOrderId = response.getLastSupplyOrderId();
            if (lastSupplyOrderId == null || lastSupplyOrderId == 0) {
                return allOrderIds;
            }

            fromSupplyOrderId = lastSupplyOrderId;
            log.info("[syncSupplyOrderList][分页获取]clientId: {}, lastSupplyOrderId: {}, 当前循环次数: {}",
                    clientId, lastSupplyOrderId, loopCount);
        }
    }

    /**
     * 同步供应订单详情
     */
    private List<SupplyOrderGetResponse.Order> syncSupplyOrderDetails(String clientId, String apiKey, List<String> orderIds) {
        List<SupplyOrderGetResponse.Order> allOrders = CollUtil.newArrayList();

        // 分批处理订单ID
        List<List<String>> orderIdBatches = CollUtil.split(orderIds, MAX_ORDER_IDS_PER_REQUEST);
        for (List<String> batchOrderIds : orderIdBatches) {
            if (!acquireRateLimit("[syncSupplyOrderDetails]", clientId)) {
                continue;
            }

            SupplyOrderGetResponse response = getSupplyOrderDetails(clientId, apiKey, batchOrderIds);
            if (!validateResponse(response, "[syncSupplyOrderDetails]", clientId)) {
                continue;
            }

            // 保存订单详情
            saveOrUpdateSupplyOrders(clientId, response);
            if (CollUtil.isNotEmpty(response.getOrders())) {
                allOrders.addAll(response.getOrders());
            }
        }

        return allOrders;
    }

    /**
     * 同步所有供应订单商品明细
     */
    private void syncAllSupplyOrderItems(String clientId, String apiKey, List<SupplyOrderGetResponse.Order> orders) {
        for (SupplyOrderGetResponse.Order order : orders) {
            if (CollUtil.isEmpty(order.getSupplies())) {
                continue;
            }
            // 获取该订单的所有供应关系
            List<OzonSupplyOrderSupplyDO> supplies = ozonSupplyOrderSupplyMapper.selectList(
                    new LambdaQueryWrapperX<OzonSupplyOrderSupplyDO>()
                            .eq(OzonSupplyOrderSupplyDO::getClientId, clientId)
                            .eq(OzonSupplyOrderSupplyDO::getSupplyOrderId, Long.valueOf(order.getSupplyOrderId())));

            if (CollUtil.isEmpty(supplies)) {
                continue;
            }

            for (OzonSupplyOrderSupplyDO supply : supplies) {
                if (supply.getBundleId() == null) {
                    continue;
                }
                if (!acquireRateLimit("[syncAllSupplyOrderItems]", clientId)) {
                    continue;
                }
                syncSupplyOrderItems(clientId, apiKey, order.getSupplyOrderId(), supply.getBundleId());
            }
        }
    }

    /**
     * 同步单个供应订单商品明细
     */
    private void syncSupplyOrderItems(String clientId, String apiKey, String orderId, String bundleId) {
        // 获取第一页数据和总数
        SupplyOrderBundleResponse firstPage = getSupplyOrderBundle(clientId, apiKey, bundleId, null);
        if (firstPage == null || firstPage.getTotalCount() == null || firstPage.getTotalCount() == 0) {
            return;
        }

        // 处理第一页数据
        int totalProcessed = processSupplyOrderItems(clientId, orderId, bundleId, firstPage);
        String lastId = firstPage.getLastId();

        // 处理剩余页数据
        while (totalProcessed < firstPage.getTotalCount() && lastId != null && !lastId.isEmpty()) {
            if (!acquireRateLimit("[syncSupplyOrderItems]", clientId)) {
                break;
            }

            SupplyOrderBundleResponse nextPage = getSupplyOrderBundle(clientId, apiKey, bundleId, lastId);
            if (nextPage == null || CollUtil.isEmpty(nextPage.getItems())) {
                break;
            }

            totalProcessed += processSupplyOrderItems(clientId, orderId, bundleId, nextPage);
            lastId = nextPage.getLastId();

            log.info("[syncSupplyOrderItems][分页获取进度]clientId: {}, orderId: {}, bundleId: {}, 已处理: {}/{}",
                    clientId, orderId, bundleId, totalProcessed, firstPage.getTotalCount());
        }
    }

    /**
     * 处理供应订单商品明细数据
     */
    private int processSupplyOrderItems(String clientId, String orderId, String bundleId, SupplyOrderBundleResponse response) {
        if (CollUtil.isEmpty(response.getItems())) {
            return 0;
        }
        saveOrUpdateSupplyOrderItems(clientId, orderId, bundleId, response);
        return response.getItems().size();
    }

    /**
     * 获取供应订单列表
     */
    private SupplyOrderListResponse getSupplyOrderList(String clientId, String apiKey, Long fromSupplyOrderId) {
        SupplyOrderListRequest request = new SupplyOrderListRequest();
        request.setClientId(clientId);
        request.setApiKey(apiKey);
        request.setPaging(new SupplyOrderListRequest.Paging()
                .setFromSupplyOrderId(fromSupplyOrderId)
                .setLimit(PAGE_SIZE));

        return ozonHttpUtil.postDirect(SUPPLY_ORDER_LIST_URL, request,
                new TypeReference<SupplyOrderListResponse>() {
                });
    }

    /**
     * 获取供应订单详情
     */
    private SupplyOrderGetResponse getSupplyOrderDetails(String clientId, String apiKey, List<String> orderIds) {
        SupplyOrderGetRequest request = new SupplyOrderGetRequest();
        request.setClientId(clientId);
        request.setApiKey(apiKey);
        request.setOrderIds(orderIds);

        return ozonHttpUtil.postDirect(SUPPLY_ORDER_GET_URL, request,
                new TypeReference<SupplyOrderGetResponse>() {
                });
    }

    /**
     * 获取供应订单商品明细
     */
    private SupplyOrderBundleResponse getSupplyOrderBundle(String clientId, String apiKey, String bundleId, String lastId) {
        SupplyOrderBundleRequest request = new SupplyOrderBundleRequest();
        request.setClientId(clientId);
        request.setApiKey(apiKey);
        request.setBundleIds(Collections.singletonList(bundleId));
        request.setLimit(PAGE_SIZE);
        request.setLastId(lastId);

        return ozonHttpUtil.postDirect(SUPPLY_ORDER_BUNDLE_URL, request,
                new TypeReference<SupplyOrderBundleResponse>() {
                });
    }

    /**
     * 检查执行限制
     */
    private boolean checkExecutionLimits(long startTime, int loopCount, String clientId) {
        if (System.currentTimeMillis() - startTime > TimeUnit.MINUTES.toMillis(MAX_EXECUTION_MINUTES)) {
            log.error("[checkExecutionLimits][执行超时，已执行{}分钟]clientId: {}", MAX_EXECUTION_MINUTES, clientId);
            return false;
        }
        if (loopCount > MAX_LOOP_COUNT) {
            log.error("[checkExecutionLimits][超过最大循环次数{}]clientId: {}", MAX_LOOP_COUNT, clientId);
            return false;
        }
        return true;
    }

    /**
     * 获取限流令牌
     */
    private boolean acquireRateLimit(String operation, String clientId) {
        try {
            boolean acquired = rateLimiter.tryAcquire(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                log.error("{}[获取令牌失败]clientId: {}", operation, clientId);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("{}[获取令牌异常]clientId: {}", operation, clientId, e);
            return false;
        }
    }

    /**
     * 验证响应结果
     */
    private boolean validateResponse(SupplyOrderListResponse response, String operation, String clientId) {
        if (response == null) {
            log.error("{}[响应为空]clientId: {}", operation, clientId);
            return false;
        }
        return true;
    }

    /**
     * 验证响应结果
     */
    private boolean validateResponse(SupplyOrderGetResponse response, String operation, String clientId) {
        if (response == null) {
            log.error("{}[响应为空]clientId: {}", operation, clientId);
            return false;
        }
        return true;
    }

    /**
     * 验证响应结果
     */
    private boolean validateResponse(SupplyOrderBundleResponse response, String operation, String clientId) {
        if (response == null) {
            log.error("{}[响应为空]clientId: {}", operation, clientId);
            return false;
        }
        return true;
    }

    // 其他原有的辅助方法保持不变
    private Long getLastSupplyOrderId(String clientId) {
        OzonSupplyOrderDO lastOrder = ozonSupplyOrderMapper.selectOne(
                new LambdaQueryWrapperX<OzonSupplyOrderDO>()
                        .eq(OzonSupplyOrderDO::getClientId, clientId)
                        .orderByDesc(OzonSupplyOrderDO::getCreateTime)
                        .last("LIMIT 1"));
        return lastOrder != null ? lastOrder.getSupplyOrderId() : 0L;
    }

    private void saveOrUpdateSupplyOrders(String clientId, SupplyOrderGetResponse response) {
        if (CollUtil.isEmpty(response.getOrders())) {
            return;
        }

        for (SupplyOrderGetResponse.Order order : response.getOrders()) {
            // 1. 保存供应订单
            OzonSupplyOrderDO orderDO = new OzonSupplyOrderDO()
                    .setClientId(clientId)
                    .setSupplyOrderId(Long.valueOf(order.getSupplyOrderId()))
                    .setSupplyOrderNumber(order.getSupplyOrderNumber())
                    .setCreationDate(LocalDate.parse(order.getCreationDate(), DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    .setState(order.getState())
                    .setWarehouseId(order.getWarehouseId())
                    .setWarehouseName(getWarehouseName(response.getWarehouses(), order.getWarehouseId()))
                    .setTimeslotFrom(LocalDateTimeUtil.of(order.getTimeslot().getValue().getTimeslot().getFrom()))
                    .setTimeslotTo(LocalDateTimeUtil.of(order.getTimeslot().getValue().getTimeslot().getTo()));

            // 更新或插入供应订单
            OzonSupplyOrderDO existingOrder = ozonSupplyOrderMapper.selectOne(
                    new LambdaQueryWrapperX<OzonSupplyOrderDO>()
                            .eq(OzonSupplyOrderDO::getSupplyOrderId, orderDO.getSupplyOrderId()));

            if (existingOrder != null) {
                orderDO.setId(existingOrder.getId());
                ozonSupplyOrderMapper.updateById(orderDO);
            } else {
                ozonSupplyOrderMapper.insert(orderDO);
            }

            // 2. 保存供应关系
            if (CollUtil.isNotEmpty(order.getSupplies())) {
                for (SupplyOrderGetResponse.Supply supply : order.getSupplies()) {
                    OzonSupplyOrderSupplyDO supplyDO = new OzonSupplyOrderSupplyDO()
                            .setClientId(clientId)
                            .setSupplyOrderId(Long.valueOf(order.getSupplyOrderId()))
                            .setSupplyId(supply.getSupplyId())
                            .setBundleId(supply.getBundleId())
                            .setStorageWarehouseId(supply.getStorageWarehouseId());

                    // 更新或插入供应关系
                    OzonSupplyOrderSupplyDO existingSupply = ozonSupplyOrderSupplyMapper.selectOne(
                            new LambdaQueryWrapperX<OzonSupplyOrderSupplyDO>()
                                    .eq(OzonSupplyOrderSupplyDO::getClientId, clientId)
                                    .eq(OzonSupplyOrderSupplyDO::getSupplyOrderId, supplyDO.getSupplyOrderId())
                                    .eq(OzonSupplyOrderSupplyDO::getSupplyId, supplyDO.getSupplyId()));

                    if (existingSupply != null) {
                        supplyDO.setId(existingSupply.getId());
                        ozonSupplyOrderSupplyMapper.updateById(supplyDO);
                    } else {
                        ozonSupplyOrderSupplyMapper.insert(supplyDO);
                    }
                }
            }
        }
    }

    private void saveOrUpdateSupplyOrderItems(String clientId, String orderId, String bundleId, SupplyOrderBundleResponse response) {
        if (CollUtil.isEmpty(response.getItems())) {
            return;
        }

        List<SupplyOrderBundleResponse.Item> items = response.getItems();
        List<String> platformSkuIds = convertList(items, SupplyOrderBundleResponse.Item::getSku)
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toList());

        Map<String, Long> platformSkuMapping = ozonProductOnlineService.batchPlatformSkuMapping(platformSkuIds);

        for (SupplyOrderBundleResponse.Item item : response.getItems()) {
            OzonSupplyOrderItemDO itemDO = new OzonSupplyOrderItemDO()
                    .setClientId(clientId)
                    .setSupplyOrderId(Long.valueOf(orderId))
                    .setBundleId(bundleId)
                    .setSku(item.getSku())
                    .setOfferId(item.getOfferId())
                    .setProductId(item.getProductId())
                    .setName(item.getName())
                    .setIconPath(item.getIconPath())
                    .setQuantity(item.getQuantity())
                    .setDmProductId(platformSkuMapping.get(String.valueOf(item.getSku())))
                    .setQuant(item.getQuant())
                    .setBarcode(item.getBarcode())
                    .setVolume(item.getVolume())
                    .setTotalVolume(item.getTotalVolume())
                    .setContractorItemCode(item.getContractorItemCode())
                    .setSfboAttribute(item.getSfboAttribute())
                    .setShipmentType(item.getShipmentType());

            // 更新或插入
            OzonSupplyOrderItemDO existingItem = ozonSupplyOrderItemMapper.selectOne(
                    new LambdaQueryWrapperX<OzonSupplyOrderItemDO>()
                            .eq(OzonSupplyOrderItemDO::getClientId, clientId)
                            .eq(OzonSupplyOrderItemDO::getSupplyOrderId, Long.valueOf(orderId))
                            .eq(OzonSupplyOrderItemDO::getSku, item.getSku()));

            if (existingItem != null) {
                itemDO.setId(existingItem.getId());
                ozonSupplyOrderItemMapper.updateById(itemDO);
            } else {
                ozonSupplyOrderItemMapper.insert(itemDO);
            }
        }
    }

    private String getWarehouseName(List<SupplyOrderGetResponse.Warehouse> warehouses, Long warehouseId) {
        if (CollUtil.isEmpty(warehouses)) {
            return null;
        }
        return warehouses.stream()
                .filter(w -> Objects.equals(w.getId(), warehouseId))
                .findFirst()
                .map(SupplyOrderGetResponse.Warehouse::getName)
                .orElse(null);
    }

    /**
     * 异步执行供应订单同步
     *
     * @param clientIds 客户端ID列表
     */
    @Async
    public void asyncSyncSupplyOrders(List<String> clientIds) {
        if (CollUtil.isEmpty(clientIds)) {
            return;
        }

        log.info("[asyncSyncSupplyOrders][开始同步供应订单] clientIds: {}", clientIds);
        long startTime = System.currentTimeMillis();

        for (String clientId : clientIds) {
            try {
                String apiKey = getApiKeyByClientId(clientId);
                if (apiKey == null) {
                    log.error("[asyncSyncSupplyOrders][获取apiKey失败] clientId: {}", clientId);
                    continue;
                }

                syncSupplyOrders(clientId, apiKey);
            } catch (Exception e) {
                log.error("[asyncSyncSupplyOrders][同步异常] clientId: {}", clientId, e);
            }
        }

        long costTime = System.currentTimeMillis() - startTime;
        log.info("[asyncSyncSupplyOrders][同步完成] clientIds: {}, 耗时: {}ms", clientIds, costTime);
    }

    /**
     * 获取apiKey
     */
    private String getApiKeyByClientId(String clientId) {
        OzonShopMappingDO shopMapping = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        if (shopMapping == null) {
            log.error("[getApiKeyByClientId][店铺不存在]clientId:{}", clientId);
            return null;
        }
        return shopMapping.getApiKey();
    }

    /**
     * 异步同步指定供应订单的商品信息
     *
     * @param clientId      客户端ID
     * @param apiKey        API密钥
     * @param supplyOrderId 供应订单ID
     */
    @Async
    public void asyncSyncSupplyOrderItems(String clientId, String apiKey, String supplyOrderId) {
        log.info("[asyncSyncSupplyOrderItems][开始同步供应订单商品] clientId: {}, supplyOrderId: {}",
                clientId, supplyOrderId);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取订单对应的所有bundle_ids
            List<OzonSupplyOrderSupplyDO> supplies = ozonSupplyOrderSupplyMapper.selectList(
                    new LambdaQueryWrapperX<OzonSupplyOrderSupplyDO>()
                            .eq(OzonSupplyOrderSupplyDO::getClientId, clientId)
                            .eq(OzonSupplyOrderSupplyDO::getSupplyOrderId, Long.valueOf(supplyOrderId)));

            if (CollUtil.isEmpty(supplies)) {
                log.warn("[asyncSyncSupplyOrderItems][未找到供应关系数据] clientId: {}, supplyOrderId: {}",
                        clientId, supplyOrderId);
                return;
            }

            // 2. 遍历每个bundle，同步商品信息
            for (OzonSupplyOrderSupplyDO supply : supplies) {
                String bundleId = supply.getBundleId();
                if (bundleId == null) {
                    continue;
                }

                if (!acquireRateLimit("[asyncSyncSupplyOrderItems]", clientId)) {
                    continue;
                }

                try {
                    syncSupplyOrderItems(clientId, apiKey, supplyOrderId, bundleId);
                } catch (Exception e) {
                    log.error("[asyncSyncSupplyOrderItems][同步单个bundle异常] clientId: {}, supplyOrderId: {}, bundleId: {}",
                            clientId, supplyOrderId, bundleId, e);
                }
            }
        } catch (Exception e) {
            log.error("[asyncSyncSupplyOrderItems][同步异常] clientId: {}, supplyOrderId: {}",
                    clientId, supplyOrderId, e);
        }

        long costTime = System.currentTimeMillis() - startTime;
        log.info("[asyncSyncSupplyOrderItems][同步完成] clientId: {}, supplyOrderId: {}, 耗时: {}ms",
                clientId, supplyOrderId, costTime);
    }

    /**
     * 异步同步指定供应订单的详细信息
     *
     * @param clientId 客户端ID
     * @param apiKey   API密钥
     * @param orderIds 供应订单ID列表
     */
    @Async
    public void asyncSyncSupplyOrderDetails(String clientId, String apiKey, List<String> orderIds) {
        log.info("[asyncSyncSupplyOrderDetails][开始同步供应订单详情] clientId: {}, orderIds: {}",
                clientId, orderIds);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取订单详情
            if (!acquireRateLimit("[asyncSyncSupplyOrderDetails]", clientId)) {
                return;
            }

            SupplyOrderGetResponse response = getSupplyOrderDetails(clientId, apiKey, orderIds);
            if (!validateResponse(response, "[asyncSyncSupplyOrderDetails]", clientId)) {
                return;
            }

            // 2. 保存订单详情和供应关系
            saveOrUpdateSupplyOrders(clientId, response);

        } catch (Exception e) {
            log.error("[asyncSyncSupplyOrderDetails][同步异常] clientId: {}, orderIds: {}",
                    clientId, orderIds, e);
        }

        long costTime = System.currentTimeMillis() - startTime;
        log.info("[asyncSyncSupplyOrderDetails][同步完成] clientId: {}, orderIds: {}, 耗时: {}ms",
                clientId, orderIds, costTime);
    }
}