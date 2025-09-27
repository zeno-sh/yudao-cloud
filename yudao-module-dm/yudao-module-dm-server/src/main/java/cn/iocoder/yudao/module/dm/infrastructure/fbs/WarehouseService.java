package cn.iocoder.yudao.module.dm.infrastructure.fbs;

import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.FbsProductStockSaveReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.FbsPushOrderLogSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsProductStockDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseAuthDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseMappingDO;
import cn.iocoder.yudao.module.dm.dto.*;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.enums.FbsPlanformEnum;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsProductStockService;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsPushOrderLogService;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsWarehouseService;
import cn.iocoder.yudao.module.dm.spi.FbsWarehousePlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.dm.infrastructure.config.OzonAsyncConfiguration.DM_THREAD_POOL_TASK_EXECUTOR;

/**
 * FBS 仓库服务
 *
 * @author: Zeno
 * @createTime: 2024/08/29 15:24
 */
@Service
@Slf4j
public class WarehouseService {

    @Resource
    private FbsWarehouseService fbsWarehouseService;
    @Resource
    private FbsProductStockService fbsProductStockService;
    @Resource
    private WarehousePluginManager warehousePluginManager;
    @Resource
    private OzonOrderService ozonOrderService;
    @Resource
    private FbsPushOrderLogService fbsPushOrderLogService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private OzonProductOnlineService productOnlineService;

    private static final List<String> CANCELED_ORDER_STATUS = Lists.newArrayList("cancelled_from_split_pending", "cancelled", "declined_by_client");

    @Async(DM_THREAD_POOL_TASK_EXECUTOR)
    public void batchPushOrder(List<Long> orderIds) {
        for (Long orderId : orderIds) {
            pushOrder(orderId);
        }
    }

    public FbsPushOrderResponse pushOrder(Long orderId) {
        FbsPushOrderResponse pushOrderResponse = new FbsPushOrderResponse();
        OzonOrderDO orderDO = ozonOrderService.getOzonOrder(orderId);
        // 订单对应的仓库信息
        String platformWarehouseId = getPlatformWarehouseId(orderDO.getDeliveryMethod());

        try {
            if (CANCELED_ORDER_STATUS.contains(orderDO.getStatus())) {
                log.warn("订单已取消，无需推送. orderId={}", orderId);
                pushOrderResponse.setMessage("订单已取消，无需推送");
                pushOrderResponse.setSuccess(false);
                return pushOrderResponse;
            }

            FbsWarehouseMappingDO fbsWarehouseMappingDO = fbsWarehouseService.getFbsWarehouseMappingByPlatformWarehouseId(platformWarehouseId);
            if (fbsWarehouseMappingDO == null) {
                throw exception(FBS_WAREHOUSE_MAPPING_NOT_EXISTS);
            }

            Long warehouseId = fbsWarehouseMappingDO.getWarehouseId();
            FbsWarehouseDO fbsWarehouseDO = fbsWarehouseService.getFbsWarehouse(warehouseId);
            if (fbsWarehouseDO == null) {
                throw exception(FBS_WAREHOUSE_NOT_EXISTS);
            }

            FbsWarehouseAuthDO fbsWarehouseAuthDO = fbsWarehouseService.getFbsWarehouseAuthByWarehouseId(warehouseId);
            if (fbsWarehouseAuthDO == null) {
                throw exception(FBS_WAREHOUSE_AUTH_NOT_EXISTS);
            }
            Integer companyId = fbsWarehouseAuthDO.getCompany();

            FbsWarehousePlugin fbsWarehousePlugin = warehousePluginManager.getPluginByCompanyId(companyId);
            FbsPushOrderRequest fbsPushOrderRequest = buildPushOrderRequest(orderDO, fbsWarehouseAuthDO);
            pushOrderResponse = fbsWarehousePlugin.pushOrder(fbsPushOrderRequest);

            savaPushLog(warehouseId, orderDO, pushOrderResponse, fbsPushOrderRequest);
            return pushOrderResponse;
        } catch (Exception e) {
            log.error("订单推送校验失败. posting_number={}", orderDO.getPostingNumber(), e);
            pushOrderResponse.setMessage(e.getMessage());
            pushOrderResponse.setSuccess(false);
        }
        savaPushLog(Long.valueOf(platformWarehouseId), orderDO, pushOrderResponse, null);
        return pushOrderResponse;
    }

    private void savaPushLog(Long warehouseId, OzonOrderDO orderDO, FbsPushOrderResponse pushOrderResponse, FbsPushOrderRequest fbsPushOrderRequest) {
        FbsPushOrderLogSaveReqVO pushOrderLogSaveReqVO = new FbsPushOrderLogSaveReqVO();
        pushOrderLogSaveReqVO.setWarehouseId(warehouseId);
        pushOrderLogSaveReqVO.setOrderId(orderDO.getId());
        pushOrderLogSaveReqVO.setPostingNumber(orderDO.getPostingNumber());
        if (fbsPushOrderRequest != null) {
            pushOrderLogSaveReqVO.setPlatformOrderId(fbsPushOrderRequest.getOrderId());
            pushOrderLogSaveReqVO.setRequest(JSON.toJSONString(fbsPushOrderRequest));
        } else {
            pushOrderLogSaveReqVO.setPlatformOrderId(orderDO.getPostingNumber());
        }
        pushOrderLogSaveReqVO.setResponse(JSON.toJSONString(pushOrderResponse));
        if (pushOrderResponse.isSuccess()) {
            pushOrderLogSaveReqVO.setStatus(Boolean.TRUE);
        } else {
            pushOrderLogSaveReqVO.setStatus(Boolean.FALSE);
        }

        fbsPushOrderLogService.createFbsPushOrderLog(pushOrderLogSaveReqVO);
    }

    /**
     * {"id":1020001321936000,"name":"Доставка Ozon самостоятельно, Люберцы","warehouse_id":1020001321936000,"warehouse":"FBS-","tpl_provider_id":24,"tpl_provider":"Доставка Ozon"}
     *
     * @param deliveryMethod
     * @return
     */
    private String getPlatformWarehouseId(String deliveryMethod) {
        JSONObject jsonObject = JSON.parseObject(deliveryMethod);
        return String.valueOf(JSONPath.eval(jsonObject, "$.warehouse_id"));
    }

    private FbsPushOrderRequest buildPushOrderRequest(OzonOrderDO orderDO, FbsWarehouseAuthDO fbsWarehouseAuthDO) {
        FbsPushOrderRequest request = new FbsPushOrderRequest();

        // 这里不同的外部系统，需要不同的请求参数
        AuthInfoDTO authInfo = new AuthInfoDTO();
        authInfo.setAppKey(fbsWarehouseAuthDO.getApiKey());
        authInfo.setAppToken(fbsWarehouseAuthDO.getToken());


        request.setAuthInfo(authInfo);
        request.setClientId(orderDO.getClientId());

        String clientId = orderDO.getClientId();
        OzonShopMappingDO shopMappingDO = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        Integer platform = shopMappingDO.getPlatform();

        //        request.setBarcode();
        request.setOrderId(orderDO.getOrderId());
        if (DmPlatformEnum.OZON.getPlatformId().equals(platform)) {
            // 不同的平台, 发货编号传的不一样
            request.setPostingNumber(orderDO.getPostingNumber());
            request.setPlatform(FbsPlanformEnum.OZON);
        } else if (DmPlatformEnum.WB.getPlatformId().equals(platform) || DmPlatformEnum.WB_GLOBAL.getPlatformId().equals(platform)) {
            request.setPostingNumber(orderDO.getOrderId());
            request.setPlatform(FbsPlanformEnum.WB);
        }

        List<OzonOrderItemDO> orderItemList = ozonOrderService.getOrderItemList(orderDO.getClientId(), orderDO.getPostingNumber(), null);
        List<FbsPushOrderItemDTO> pushItemList = new ArrayList<>();
        for (OzonOrderItemDO ozonOrderItemDO : orderItemList) {
            FbsPushOrderItemDTO pushOrderItemDTO = new FbsPushOrderItemDTO();
            pushOrderItemDTO.setSkuId(getFbsProductSku(clientId, ozonOrderItemDO.getOfferId(), fbsWarehouseAuthDO.getWarehouseId()));
            pushOrderItemDTO.setQuantity(ozonOrderItemDO.getQuantity());
            pushItemList.add(pushOrderItemDTO);
        }

        request.setItems(pushItemList);
        return request;
    }

    private String getFbsProductSku(String clientId, String offerId, Long warehouseId) {
        Long dmProductId = getDmProductId(clientId, offerId);
        FbsProductStockDO fbsProductStockDO = fbsProductStockService.getFbsProductStockByWarehouseIdAndProductId(warehouseId, dmProductId);
        if (fbsProductStockDO == null) {
            throw exception(FBS_PRODUCT_STOCK_MAPPING_NOT_EXISTS);
        }

        return fbsProductStockDO.getProductSku();
    }

    private Long getDmProductId(String clientId, String offerId) {
        OzonProductOnlineDO productOnlineDO = productOnlineService.getOzonProductOnlineByOfferId(clientId, offerId);
        if (productOnlineDO == null) {
            throw exception(OZON_PRODUCT_ONLINE_NOT_EXISTS);
        }

        if (productOnlineDO.getDmProductId() == null) {
            throw exception(OZON_PRODUCT_ONLINE_MAPPING_NOT_EXISTS);
        }

        return productOnlineDO.getDmProductId();
    }

    public void syncStock(Long warehouseId) {
        FbsWarehouseAuthDO fbsWarehouseAuth = fbsWarehouseService.getFbsWarehouseAuthByWarehouseId(warehouseId);
        FbsWarehousePlugin fbsWarehousePlugin = warehousePluginManager.getPluginByCompanyId(fbsWarehouseAuth.getCompany());
        FbsStockResponse response = fbsWarehousePlugin.syncStock(buildRequest(fbsWarehouseAuth));
        saveStock(warehouseId, response);
    }


    private void saveStock(Long warehouseId, FbsStockResponse response) {
        if (null != response && CollectionUtils.isNotEmpty(response.getStockItems())) {
            List<FbsProductStockDTO> stockDTOList = response.getStockItems();
            for (FbsProductStockDTO stockDTO : stockDTOList) {
                FbsProductStockSaveReqVO reqVO = new FbsProductStockSaveReqVO();
                reqVO.setWarehouseId(warehouseId);
                reqVO.setProductSku(stockDTO.getFbsSku());
                reqVO.setOnway(stockDTO.getOnway());
                reqVO.setSellable(stockDTO.getSellable());
                reqVO.setShipped(stockDTO.getShipped());
                fbsProductStockService.createFbsProductStock(reqVO);
            }
        }
    }

    private FbsStockRequest buildRequest(FbsWarehouseAuthDO fbsWarehouseAuthDO) {
        FbsStockRequest request = new FbsStockRequest();

        AuthInfoDTO authInfoDTO = new AuthInfoDTO();
        authInfoDTO.setAppToken(fbsWarehouseAuthDO.getToken());
        authInfoDTO.setAppKey(fbsWarehouseAuthDO.getApiKey());
        request.setAuthInfo(authInfoDTO);

        return request;
    }
}
