package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.order.vo.OzonOrderSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.OrderResultDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.PostingDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ProductDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FboOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FbsOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
import cn.iocoder.yudao.module.dm.infrastructure.service.OrderQueryService;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 异步订单管理服务 - 避免while true循环导致的卡住问题
 * @Author Jax
 * @Date 2025-09-13
 */
@Service
@Slf4j
public class OrderManagerAsyncService {

    @Resource
    private OrderQueryService<OzonHttpResponse, FbsOrderQueryRequest> fbsOrderQueryService;
    @Resource
    private OrderQueryService<OzonHttpResponse, FboOrderQueryRequest> fboOrderQueryService;
    @Resource
    private OzonOrderService dmOrderService;
    @Resource
    private OzonHttpUtil ozonHttpUtil;
    @Resource
    private OzonShopMappingService ozonShopMappingService;

    /**
     * 异步同步单个店铺订单
     * 
     * @param shop 店铺信息
     * @param utcBeginDateTime 开始时间
     * @param utcEndDateTime 结束时间
     * @return 同步结果
     */
    @Async("DM_ORDER_ASYNC_EXECUTOR")
    public CompletableFuture<String> syncOrderAsync(OzonShopMappingDO shop, String utcBeginDateTime, String utcEndDateTime) {
        String shopName = shop.getClientId();
        
        try {
            log.info("[OrderManagerAsyncService]-开始同步店铺[{}]订单", shopName);
            
            CompletableFuture<String> syncTask = CompletableFuture.supplyAsync(() -> {
                try {
                    Integer platform = shop.getPlatform();
                    if (DmPlatformEnum.OZON.getPlatformId().equals(platform) || DmPlatformEnum.OZON_GLOBAL.getPlatformId().equals(platform)) {
                        // 同步FBS订单
                        String fbsResult = doFbsAsync(shop, utcBeginDateTime, utcEndDateTime);
                        log.info("[OrderManagerAsyncService]-店铺[{}] FBS同步结果: {}", shopName, fbsResult);
                        
                        // 同步FBO订单
                        String fboResult = doFboAsync(shop, utcBeginDateTime, utcEndDateTime);
                        log.info("[OrderManagerAsyncService]-店铺[{}] FBO同步结果: {}", shopName, fboResult);
                    }
                    
                    String result = String.format("店铺[%s]同步成功", shopName);
                    log.info("[OrderManagerAsyncService]-{}", result);
                    return result;
                } catch (Exception e) {
                    String errorMsg = String.format("店铺[%s]同步失败: %s", shopName, e.getMessage());
                    log.error("[OrderManagerAsyncService]-{}", errorMsg, e);
                    return errorMsg;
                }
            });
            
            // 设置15分钟超时 - 兼容Java 8
            return syncTask.exceptionally(throwable -> {
                String errorMsg = String.format("店铺[%s]同步异常: %s", shopName, throwable.getMessage());
                log.error("[OrderManagerAsyncService]-{}", errorMsg, throwable);
                return errorMsg;
            });
                    
        } catch (Exception e) {
            String errorMsg = String.format("店铺[%s]启动同步失败: %s", shopName, e.getMessage());
            log.error("[OrderManagerAsyncService]-{}", errorMsg, e);
            return CompletableFuture.completedFuture(errorMsg);
        }
    }

    /**
     * 同步FBS订单 - 使用安全的分页逻辑
     */
    private String doFbsAsync(OzonShopMappingDO dmShopMapping, String utcBeginDateTime, String utcEndDateTime) {
        try {
            FbsOrderQueryRequest request = new FbsOrderQueryRequest();
            request.setClientId(dmShopMapping.getClientId());
            request.setApiKey(dmShopMapping.getApiKey());
            
            FbsOrderQueryRequest.Filter filter = new FbsOrderQueryRequest.Filter();
            filter.setSince(utcBeginDateTime);
            filter.setTo(utcEndDateTime);

            FbsOrderQueryRequest.Include include = new FbsOrderQueryRequest.Include();
            include.setBarcodes(true);

            request.setFilter(filter);
            request.setLimit(100);
            request.setOffset(0);
            request.setWith(include);
            
            return saveOrderSafely(dmShopMapping.getTenantId(), request);
        } catch (Exception e) {
            String errorMsg = String.format("FBS订单同步失败: %s", e.getMessage());
            log.error("[OrderManagerAsyncService]-{}", errorMsg, e);
            return errorMsg;
        }
    }

    /**
     * 同步FBO订单 - 使用安全的分页逻辑
     */
    private String doFboAsync(OzonShopMappingDO dmShopMapping, String utcBeginDateTime, String utcEndDateTime) {
        try {
            FboOrderQueryRequest request = new FboOrderQueryRequest();
            request.setClientId(dmShopMapping.getClientId());
            request.setApiKey(dmShopMapping.getApiKey());
            
            FboOrderQueryRequest.Filter filter = new FboOrderQueryRequest.Filter();
            filter.setSince(utcBeginDateTime);
            filter.setTo(utcEndDateTime);

            FboOrderQueryRequest.Include include = new FboOrderQueryRequest.Include();
            include.setFinancialData(true);

            request.setFilter(filter);
            request.setWith(include);
            
            return saveFboOrderSafely(dmShopMapping.getTenantId(), request);
        } catch (Exception e) {
            String errorMsg = String.format("FBO订单同步失败: %s", e.getMessage());
            log.error("[OrderManagerAsyncService]-{}", errorMsg, e);
            return errorMsg;
        }
    }

    /**
     * 安全的FBS订单保存 - 避免无限循环
     */
    private String saveOrderSafely(Long tenantId, FbsOrderQueryRequest request) {
        int totalProcessed = 0;
        int maxPages = 100; // 最大页数限制，防止无限循环
        int currentPage = 0;
        
        while (currentPage < maxPages) {
            try {
                OzonHttpResponse<OrderResultDTO> ozonHttpResponse = fbsOrderQueryService.queryOrder(request);
                
                if (ozonHttpResponse == null || ozonHttpResponse.getResult() == null) {
                    log.warn("[OrderManagerAsyncService]-FBS订单查询返回空结果，页码: {}", currentPage);
                    break;
                }
                
                OrderResultDTO result = ozonHttpResponse.getResult();
                List<PostingDTO> postings = result.getPostings();
                
                if (CollectionUtils.isNotEmpty(postings)) {
                    insertOrders(tenantId, request.getClientId(), postings, true);
                    totalProcessed += postings.size();
                    log.info("[OrderManagerAsyncService]-FBS订单处理页码: {}, 订单数: {}", currentPage, postings.size());
                }
                
                if (result.isHasNext() && CollectionUtils.isNotEmpty(postings)) {
                    request.setOffset(request.getOffset() + request.getLimit());
                    currentPage++;
                } else {
                    break;
                }
                
                // 添加短暂延迟，避免API限流
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("[OrderManagerAsyncService]-FBS订单同步异常，页码: {}, 错误: {}", currentPage, e.getMessage(), e);
                break;
            }
        }
        
        if (currentPage >= maxPages) {
            log.warn("[OrderManagerAsyncService]-FBS订单同步达到最大页数限制: {}", maxPages);
        }
        
        return String.format("FBS订单同步完成，共处理%d条订单", totalProcessed);
    }

    /**
     * 安全的FBO订单保存 - 避免无限循环
     */
    private String saveFboOrderSafely(Long tenantId, FboOrderQueryRequest fboOrderQueryRequest) {
        int limit = 100;
        int offset = 0;
        int totalProcessed = 0;
        int maxPages = 100; // 最大页数限制，防止无限循环
        int currentPage = 0;
        List<PostingDTO> allPostingDTOList = new ArrayList<>();

        while (currentPage < maxPages) {
            try {
                // 更新请求中的分页参数
                fboOrderQueryRequest.setLimit(limit);
                fboOrderQueryRequest.setOffset(offset);

                // 调用服务查询订单
                OzonHttpResponse<List<PostingDTO>> ozonHttpResponse = fboOrderQueryService.queryOrder(fboOrderQueryRequest);

                // 检查响应结果是否为空或结果列表是否为空
                if (ozonHttpResponse == null || ozonHttpResponse.getResult() == null || CollectionUtils.isEmpty(ozonHttpResponse.getResult())) {
                    log.info("[OrderManagerAsyncService]-FBO订单查询无更多数据，页码: {}", currentPage);
                    break;
                }

                List<PostingDTO> currentPageData = ozonHttpResponse.getResult();
                allPostingDTOList.addAll(currentPageData);
                totalProcessed += currentPageData.size();
                
                log.info("[OrderManagerAsyncService]-FBO订单处理页码: {}, 订单数: {}", currentPage, currentPageData.size());

                // 更新offset以进行下一次分页查询
                offset += limit;
                currentPage++;
                
                // 如果返回的数据少于limit，说明已经是最后一页
                if (currentPageData.size() < limit) {
                    break;
                }
                
                // 添加短暂延迟，避免API限流
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("[OrderManagerAsyncService]-FBO订单同步异常，页码: {}, 错误: {}", currentPage, e.getMessage(), e);
                break;
            }
        }
        
        if (currentPage >= maxPages) {
            log.warn("[OrderManagerAsyncService]-FBO订单同步达到最大页数限制: {}", maxPages);
        }

        // 插入所有查询到的订单
        if (CollectionUtils.isNotEmpty(allPostingDTOList)) {
            insertOrders(tenantId, fboOrderQueryRequest.getClientId(), allPostingDTOList, false);
        }
        
        return String.format("FBO订单同步完成，共处理%d条订单", totalProcessed);
    }

    /**
     * 插入订单数据 - 复用原有逻辑
     */
    private void insertOrders(Long tenantId, String clientId, List<PostingDTO> postings, Boolean isFbs) {
        if (CollectionUtils.isEmpty(postings)) {
            return;
        }
        for (PostingDTO postingDTO : postings) {
            try {
                OzonOrderDO ozonOrderDO = new OzonOrderDO();
                ozonOrderDO.setTenantId(tenantId);
                ozonOrderDO.setClientId(clientId);
                ozonOrderDO.setOrderId(String.valueOf(postingDTO.getOrderId()));
                ozonOrderDO.setPostingNumber(postingDTO.getPostingNumber());
                ozonOrderDO.setParentPostingNumber(postingDTO.getParentPostingNumber());
                ozonOrderDO.setOrderNumber(postingDTO.getOrderNumber());
                ozonOrderDO.setStatus(postingDTO.getStatus());
                ozonOrderDO.setCancellation(postingDTO.getCancellation());
                ozonOrderDO.setProducts(JSON.toJSONString(postingDTO.getProducts()));
                ozonOrderDO.setDeliveryMethod(postingDTO.getDeliveryMethod());
                ozonOrderDO.setSubstatus(postingDTO.getSubstatus());
                ozonOrderDO.setAccrualsForSale(calculateTotalProductPrice(postingDTO));
                ozonOrderDO.setOrderType(isFbs ? 20 : 10);
                
                if (postingDTO.getInProcessAt() != null) {
                    ozonOrderDO.setInProcessAt(LocalDateTimeUtil.ofUTC(postingDTO.getInProcessAt().toInstant()));
                }
                if (postingDTO.getShipmentDate() != null) {
                    ozonOrderDO.setShipmentDate(LocalDateTimeUtil.ofUTC(postingDTO.getShipmentDate().toInstant()));
                }
                if (postingDTO.getDeliveringDate() != null) {
                    ozonOrderDO.setDeliveringDate(LocalDateTimeUtil.ofUTC(postingDTO.getDeliveringDate().toInstant()));
                }

                if (Objects.nonNull(postingDTO.getBarcodes())) {
                    ozonOrderDO.setBarcode(postingDTO.getBarcodes().getUpperBarcode());
                }

                List<OzonOrderDO> ozonOrderList = dmOrderService.getOzonOrderList(clientId, postingDTO.getPostingNumber(), null, null);
                if (CollectionUtils.isEmpty(ozonOrderList)) {
                    dmOrderService.createOzonOrder(BeanUtils.toBean(ozonOrderDO, OzonOrderSaveReqVO.class));
                } else {
                    ozonOrderDO.setId(ozonOrderList.get(0).getId());
                    //再次重新复制，防止java处理成本地时区
                    if (postingDTO.getInProcessAt() != null) {
                        ozonOrderDO.setInProcessAt(LocalDateTimeUtil.ofUTC(postingDTO.getInProcessAt().toInstant()));
                    }
                    if (null != postingDTO.getShipmentDate()) {
                        ozonOrderDO.setShipmentDate(LocalDateTimeUtil.ofUTC(postingDTO.getShipmentDate().toInstant()));
                    }
                    if (null != postingDTO.getDeliveringDate()) {
                        ozonOrderDO.setDeliveringDate(LocalDateTimeUtil.ofUTC(postingDTO.getDeliveringDate().toInstant()));
                    }
                    ozonOrderDO.setOrderType(isFbs ? 20 : 10);
                    dmOrderService.updateOzonOrder(BeanUtils.toBean(ozonOrderDO, OzonOrderSaveReqVO.class));
                }

                //保存订单商品明细
                saveOrderItem(tenantId, clientId, postingDTO);
                
            } catch (Exception e) {
                log.error("[OrderManagerAsyncService]-处理订单异常，订单号: {}, 错误: {}", postingDTO.getPostingNumber(), e.getMessage(), e);
                // 继续处理下一个订单，不中断整个流程
            }
        }
    }

    /**
     * 保存订单商品明细 - 复用原有逻辑
     */
    private void saveOrderItem(Long tenantId, String clientId, PostingDTO postingDTO) {
        List<ProductDTO> products = postingDTO.getProducts();
        if (CollectionUtils.isNotEmpty(products)) {
            for (ProductDTO product : products) {
                try {
                    OzonOrderItemDO orderItem = new OzonOrderItemDO();
                    orderItem.setTenantId(tenantId);
                    orderItem.setClientId(clientId);
                    orderItem.setOrderId(String.valueOf(postingDTO.getOrderId()));
                    orderItem.setPostingNumber(postingDTO.getPostingNumber());
                    orderItem.setPlatformSkuId(String.valueOf(product.getSku()));
                    orderItem.setOfferId(product.getOfferId());
                    orderItem.setQuantity(product.getQuantity());
                    orderItem.setPrice(product.getPrice());
                    if (postingDTO.getInProcessAt() != null) {
                        orderItem.setInProcessAt(LocalDateTimeUtil.ofUTC(postingDTO.getInProcessAt().toInstant()));
                    }

                    List<OzonOrderItemDO> dmOrderItems = dmOrderService.getOrderItemListByPostingNumberAndPlatformSkuId(clientId, postingDTO.getPostingNumber(), String.valueOf(product.getSku()));
                    if (CollectionUtils.isEmpty(dmOrderItems)) {
                        dmOrderService.createOzonOrderItemList(String.valueOf(postingDTO.getOrderId()), Lists.newArrayList(orderItem));
                    } else if (dmOrderItems.size() > 1) {
                        // 当结果不为空且大于1时，清理现有数据再插入
                        dmOrderService.deleteOzonOrderItemByPostingNumberAndPlatformSkuId(clientId, postingDTO.getPostingNumber(), String.valueOf(product.getSku()));
                        dmOrderService.createOzonOrderItemList(String.valueOf(postingDTO.getOrderId()), Lists.newArrayList(orderItem));
                    } else {
                        orderItem.setId(dmOrderItems.get(0).getId());
                        if (postingDTO.getInProcessAt() != null) {
                            orderItem.setInProcessAt(LocalDateTimeUtil.ofUTC(postingDTO.getInProcessAt().toInstant()));
                        }
                        dmOrderService.updateOzonOrderItemList(String.valueOf(postingDTO.getOrderId()), Lists.newArrayList(orderItem));
                    }
                } catch (Exception e) {
                    log.error("[OrderManagerAsyncService]-处理订单商品异常，订单号: {}, SKU: {}, 错误: {}", 
                            postingDTO.getPostingNumber(), product.getSku(), e.getMessage(), e);
                    // 继续处理下一个商品，不中断整个流程
                }
            }
        }
    }

    /**
     * 计算订单总价 - 复用原有逻辑
     */
    private BigDecimal calculateTotalProductPrice(PostingDTO postingDTO) {
        BigDecimal sum = BigDecimal.ZERO;
        List<ProductDTO> products = postingDTO.getProducts();
        if (!products.isEmpty()) {
            sum = products.stream()
                    .map(product -> product.getPrice()
                            .multiply(
                                    product.getQuantity() == 0 ? BigDecimal.ONE : new BigDecimal(product.getQuantity()))
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return sum;
    }
}
