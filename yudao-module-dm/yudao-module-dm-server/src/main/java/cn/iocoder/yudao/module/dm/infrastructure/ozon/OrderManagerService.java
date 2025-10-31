package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.order.vo.OzonOrderSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.IncludeDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.OrderResultDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.PostingDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ProductDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FboOrderInfoRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FboOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FbsOrderInfoRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FbsOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
import cn.iocoder.yudao.module.dm.infrastructure.service.OrderQueryService;
import cn.iocoder.yudao.module.dm.infrastructure.wb.WbOrderManagerService;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Service
@Slf4j
public class OrderManagerService {

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
    @Resource
    private WbOrderManagerService wbOrderManagerService;

    public void syncOrder(String clientId, String utcBeginDateTime, String utcEndDateTime) {
        log.info("开始同步订单");
        OzonShopMappingDO dmShopMapping = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        if (null == dmShopMapping) {
            log.error("没有找到店铺信息");
            return;
        }

        Integer platform = dmShopMapping.getPlatform();
        if (DmPlatformEnum.OZON.getPlatformId().equals(platform)) {
            doFbs(dmShopMapping, utcBeginDateTime, utcEndDateTime);
            doFbo(dmShopMapping, utcBeginDateTime, utcEndDateTime);
        } else if (DmPlatformEnum.WB.getPlatformId().equals(platform) || DmPlatformEnum.WB_GLOBAL.getPlatformId().equals(platform)) {
            wbOrderManagerService.syncOrder(clientId, utcBeginDateTime, utcEndDateTime);
        }
    }

    public void updateOrderInfo(String clientId, String postingNumber) {
        OzonShopMappingDO dmShopMapping = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        FbsOrderInfoRequest fbsOrderInfoRequest = new FbsOrderInfoRequest();
        fbsOrderInfoRequest.setApiKey(dmShopMapping.getApiKey());
        fbsOrderInfoRequest.setClientId(dmShopMapping.getClientId());
        fbsOrderInfoRequest.setPostingNumber(postingNumber);
        saveOrderByInfo(dmShopMapping.getTenantId(), fbsOrderInfoRequest);
    }

    private void doFbs(OzonShopMappingDO dmShopMapping, String utcBeginDateTime, String utcEndDateTime) {
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
        saveOrder(dmShopMapping.getTenantId(), request);
    }

    private void doFbo(OzonShopMappingDO dmShopMapping, String utcBeginDateTime, String utcEndDateTime) {
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
        saveFboOrder(dmShopMapping.getTenantId(), request);
    }

    public List<OzonOrderDO> getPayOrders(String clientId, String beginDate, String endDate) {
        LocalDateTime beginDateTime = DateUtil.parseLocalDateTime(beginDate, DatePattern.NORM_DATETIME_PATTERN);
        LocalDateTime endDateTime = DateUtil.parseLocalDateTime(endDate, DatePattern.NORM_DATETIME_PATTERN);
        return dmOrderService.getOzonOrderList(clientId, null, beginDateTime, endDateTime);
    }

    public void saveOrderByInfo(Long tenantId, FbsOrderInfoRequest request) {
        OzonHttpResponse<PostingDTO> response = getPostingInfo(request);
        if (response != null && response.getResult() != null) {
            PostingDTO postingDTO = response.getResult();
            insertOrders(tenantId, request.getClientId(), Lists.newArrayList(postingDTO), true);
        }
    }

    public void saveFboOrderByInfo(Long tenantId, FboOrderInfoRequest request) {
        OzonHttpResponse<PostingDTO> response = getFboPostingInfo(request);
        if (response != null && response.getResult() != null) {
            PostingDTO postingDTO = response.getResult();
            insertOrders(tenantId, request.getClientId(), Lists.newArrayList(postingDTO), false);
        }
    }

    public void saveOrder(Long tenantId, FbsOrderQueryRequest request) {
        while (true) {
            OzonHttpResponse<OrderResultDTO> ozonHttpResponse = fbsOrderQueryService.queryOrder(request);
            if (ozonHttpResponse != null && ozonHttpResponse.getResult() != null) {
                OrderResultDTO result = ozonHttpResponse.getResult();
                List<PostingDTO> postings = result.getPostings();
                if (CollectionUtils.isNotEmpty(postings)) {
                    // 保存订单信息
                    insertOrders(tenantId, request.getClientId(), postings, true);
                }
                if (result.isHasNext()) {
                    request.setOffset(request.getOffset() + request.getLimit());
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    public void saveFboOrderOld(Long tenantId, FboOrderQueryRequest fboOrderQueryRequest) {
        OzonHttpResponse<List<PostingDTO>> ozonHttpResponse = fboOrderQueryService.queryOrder(fboOrderQueryRequest);
        if (ozonHttpResponse != null && ozonHttpResponse.getResult() != null && CollectionUtils.isNotEmpty(ozonHttpResponse.getResult())) {
            List<PostingDTO> postingDTOList = ozonHttpResponse.getResult();
            insertOrders(tenantId, fboOrderQueryRequest.getClientId(), postingDTOList, false);
        }
    }

    public void saveFboOrder(Long tenantId, FboOrderQueryRequest fboOrderQueryRequest) {
        int limit = 100;
        int offset = 0;
        List<PostingDTO> allPostingDTOList = new ArrayList<>();

        while (true) {
            // 更新请求中的分页参数
            fboOrderQueryRequest.setLimit(limit);
            fboOrderQueryRequest.setOffset(offset);

            // 调用服务查询订单
            OzonHttpResponse<List<PostingDTO>> ozonHttpResponse = fboOrderQueryService.queryOrder(fboOrderQueryRequest);

            // 检查响应结果是否为空或结果列表是否为空
            if (ozonHttpResponse == null || ozonHttpResponse.getResult() == null || CollectionUtils.isEmpty(ozonHttpResponse.getResult())) {
                break;
            }

            // 将当前查询结果添加到总结果列表中
            allPostingDTOList.addAll(ozonHttpResponse.getResult());

            // 更新offset以进行下一次分页查询
            offset += limit;
        }

        // 插入所有查询到的订单
        if (CollectionUtils.isNotEmpty(allPostingDTOList)) {
            insertOrders(tenantId, fboOrderQueryRequest.getClientId(), allPostingDTOList, false);
        }
    }


    public void updateOrderStatus(String clientId, String postingNumber, String newStatus, String subStatus) {

        List<OzonOrderDO> dmOrders = dmOrderService.getOzonOrderList(clientId, postingNumber, null, null);
        if (CollectionUtils.isEmpty(dmOrders)) {
            return;
        }

        OzonOrderDO dmOrder = dmOrders.get(0);
        dmOrder.setStatus(newStatus);
        dmOrder.setSubstatus(subStatus);

        List<OzonOrderItemDO> ozonOrderItemList = dmOrderService.getOrderItemList(clientId, postingNumber, null);

        OzonOrderSaveReqVO updateReqVO = BeanUtils.toBean(dmOrder, OzonOrderSaveReqVO.class);
        updateReqVO.setOzonOrderItems(ozonOrderItemList);
        dmOrderService.updateOzonOrder(updateReqVO);
    }

    private OzonHttpResponse<PostingDTO> getPostingInfo(FbsOrderInfoRequest request) {
        IncludeDTO includeDTO = new IncludeDTO();
        includeDTO.setFinancialData(true);
        request.setInclude(includeDTO);
        TypeReference<OzonHttpResponse<PostingDTO>> typeReference = new TypeReference<OzonHttpResponse<PostingDTO>>() {
        };

        return ozonHttpUtil.post(OzonConfig.OZON_ORDER_POSTING_INFO, request, typeReference);
    }

    private OzonHttpResponse<PostingDTO> getFboPostingInfo(FboOrderInfoRequest request) {
        IncludeDTO includeDTO = new IncludeDTO();
        includeDTO.setFinancialData(true);
        request.setInclude(includeDTO);
        TypeReference<OzonHttpResponse<PostingDTO>> typeReference = new TypeReference<OzonHttpResponse<PostingDTO>>() {
        };

        return ozonHttpUtil.post(OzonConfig.OZON_ORDER_FBO_POSTING_INFO, request, typeReference);
    }

    private void insertOrders(Long tenantId, String clientId, List<PostingDTO> postings, Boolean isFbs) {
        if (CollectionUtils.isEmpty(postings)) {
            return;
        }
        for (PostingDTO postingDTO : postings) {
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
                ozonOrderDO.setInProcessAt(LocalDateTimeUtil.ofUTC(postingDTO.getInProcessAt().toInstant()));
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

        }
    }

    private void saveOrderItem(Long tenantId, String clientId, PostingDTO postingDTO) {
        List<ProductDTO> products = postingDTO.getProducts();
        if (CollectionUtils.isNotEmpty(products)) {
            for (ProductDTO product : products) {
                OzonOrderItemDO orderItem = new OzonOrderItemDO();
                orderItem.setTenantId(tenantId);
                orderItem.setClientId(clientId);
                orderItem.setOrderId(String.valueOf(postingDTO.getOrderId()));
                orderItem.setPostingNumber(postingDTO.getPostingNumber());
                orderItem.setPlatformSkuId(String.valueOf(product.getSku()));
                orderItem.setOfferId(product.getOfferId());
                orderItem.setQuantity(product.getQuantity());
                orderItem.setPrice(product.getPrice());
                orderItem.setInProcessAt(LocalDateTimeUtil.ofUTC(postingDTO.getInProcessAt().toInstant()));

                List<OzonOrderItemDO> dmOrderItems = dmOrderService.getOrderItemListByPostingNumberAndPlatformSkuId(clientId, postingDTO.getPostingNumber(), String.valueOf(product.getSku()));
                if (CollectionUtils.isEmpty(dmOrderItems)) {
                    dmOrderService.createOzonOrderItemList(String.valueOf(postingDTO.getOrderId()), Lists.newArrayList(orderItem));
                } else if (dmOrderItems.size() > 1) {
                    // 当结果不为空且大于1时，清理现有数据再插入
                    dmOrderService.deleteOzonOrderItemByPostingNumberAndPlatformSkuId(clientId, postingDTO.getPostingNumber(), String.valueOf(product.getSku()));
                    dmOrderService.createOzonOrderItemList(String.valueOf(postingDTO.getOrderId()), Lists.newArrayList(orderItem));
                } else {
                    orderItem.setId(dmOrderItems.get(0).getId());
                    orderItem.setInProcessAt(LocalDateTimeUtil.ofUTC(postingDTO.getInProcessAt().toInstant()));
                    dmOrderService.updateOzonOrderItemList(String.valueOf(postingDTO.getOrderId()), Lists.newArrayList(orderItem));
                }
            }
        }
    }


    private BigDecimal calculateTotalProductPrice(PostingDTO postingDTO) {
        BigDecimal sum = BigDecimal.ZERO; // 使用BigDecimal.ZERO初始化总和
        List<ProductDTO> products = postingDTO.getProducts();
        if (!products.isEmpty()) { // 检查产品列表是否为空
            // 使用stream进行累加
            sum = products.stream()
                    .map(product -> product.getPrice()
                            .multiply(
                                    product.getQuantity() == 0 ? BigDecimal.ONE : new BigDecimal(product.getQuantity()))
                    ) // 获取价格并乘以数量
                    .reduce(BigDecimal.ZERO, BigDecimal::add); // 使用reduce方法累加
        }
        return sum;
    }
}
