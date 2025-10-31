package cn.iocoder.yudao.module.dm.infrastructure.ozon.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.online.vo.OzonProductOnlineSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OnlineProductStatusEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ProductOnlineDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ProductOnlineItemDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ProductOnlineResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ProductResultDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.HttpBaseRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.SyncProductOnlineRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProductOnlineSyncService;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * @author: Zeno
 * @createTime: 2024/07/13 18:40
 */
@Service("ozonProductOnlineSyncService")
@Slf4j
public class OzonProductOnlineSyncServiceImpl implements ProductOnlineSyncService {

    @Resource
    private OzonHttpUtil<HttpBaseRequest> ozonHttpUtil;
    @Resource
    private OzonProductOnlineService productOnlineService;
    
    // 每批处理的商品数量
    private static final int BATCH_SIZE = 50;

    @Override
    public void sync(OzonShopMappingDO shopMappingDO) {
        SyncProductOnlineRequest request = new SyncProductOnlineRequest();
        request.setClientId(shopMappingDO.getClientId());
        request.setApiKey(shopMappingDO.getApiKey());
        request.setLimit(1000);
        request.setFilter(new HashMap<>());
        OzonHttpResponse<ProductResultDTO> response = getOnlineProducts(request);
        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getResult().getItems())) {
            log.error("查询不到对应产品信息");
            return;
        }

        // 查本地所有与线上diff
        List<ProductOnlineItemDTO> items = response.getResult().getItems();

        List<OzonProductOnlineDO> allProductOnlineList = productOnlineService.getAllProductOnlineByClientId(shopMappingDO.getClientId());
        
        // 清理重复的platformSkuId记录
        cleanDuplicatePlatformSkuIdRecords(allProductOnlineList, shopMappingDO.getClientId());
        
        // 重新获取清理后的记录
        allProductOnlineList = productOnlineService.getAllProductOnlineByClientId(shopMappingDO.getClientId());
        List<String> localProductIds = convertList(allProductOnlineList, OzonProductOnlineDO::getProductId);
        List<String> onlineProductIds = convertList(items, ProductOnlineItemDTO::getProductId);

        // 找出本地有但线上列表中没有的商品ID
        Set<String> onlineProductIdSet = new HashSet<>(onlineProductIds);
        List<String> diffProductIds = localProductIds.stream()
                .filter(id -> !onlineProductIdSet.contains(id))
                .collect(Collectors.toList());
        
        // 处理所有商品ID，包括线上返回的ID和本地差异的ID
        Set<String> allProductIdsToProcess = new HashSet<>(onlineProductIds);
        if (CollectionUtils.isNotEmpty(diffProductIds)) {
            allProductIdsToProcess.addAll(diffProductIds);
            log.info("发现本地存在但线上列表中不存在的商品数量: {}, 将进一步查询确认， {}", diffProductIds.size(), JSON.toJSONString(diffProductIds));
        }
        
        log.info("开始处理总共 {} 个商品信息", allProductIdsToProcess.size());
        
        // 构建productId到商品记录的映射，便于快速查找本地记录
        Map<String, OzonProductOnlineDO> productIdToRecord = allProductOnlineList.stream()
                .collect(Collectors.toMap(OzonProductOnlineDO::getProductId, record -> record, (existing, replacement) -> existing));
        
        // 将商品ID列表分批处理，每批最多50个
        List<List<String>> batchedProductIds = Lists.partition(new ArrayList<>(allProductIdsToProcess), BATCH_SIZE);
        
        for (List<String> batchProductIds : batchedProductIds) {
            SyncProductOnlineRequest productOnlineRequest = new SyncProductOnlineRequest();
            productOnlineRequest.setClientId(shopMappingDO.getClientId());
            productOnlineRequest.setApiKey(shopMappingDO.getApiKey());
            productOnlineRequest.setProductId(batchProductIds);

            ProductOnlineResponse productOnlineResponse = getOnlineProductInfo2(productOnlineRequest);
            if (Objects.isNull(productOnlineResponse) || CollectionUtils.isEmpty(productOnlineResponse.getItems())) {
                log.error("批量查询商品信息失败，批次大小: {}", batchProductIds.size());
                
                // 如果批量查询失败，可能是因为此批次中的商品都已不存在
                // 将这批次中的本地存在的商品标记为归档
                handleBatchArchivedProducts(batchProductIds, productIdToRecord, shopMappingDO);
                continue;
            }
            
            // 获取此批次返回结果中的商品ID
            Set<String> returnedProductIds = productOnlineResponse.getItems().stream()
                    .map(dto -> dto.getId().toString())
                    .collect(Collectors.toSet());
            
            // 找出此批次中查询不到的商品ID
            List<String> notFoundProductIds = batchProductIds.stream()
                    .filter(id -> !returnedProductIds.contains(id))
                    .filter(id -> productIdToRecord.containsKey(id)) // 只处理本地存在的记录
                    .collect(Collectors.toList());
            
            // 处理查询不到的商品，将其标记为归档
            if (CollectionUtils.isNotEmpty(notFoundProductIds)) {
                log.info("批次中发现 {} 个商品在线上查询不到，将标记为已归档", notFoundProductIds.size());
                for (String productId : notFoundProductIds) {
                    markProductAsArchived(productId, productIdToRecord.get(productId), shopMappingDO.getClientId());
                }
            }
            
            // 批量处理获取到的商品信息（更新或新增）
            processProductOnlineItems(productOnlineResponse.getItems(), shopMappingDO);
        }
    }
    
    /**
     * 标记商品为已归档
     * 
     * @param productId 商品ID
     * @param product 商品记录
     * @param clientId 客户端ID
     */
    private void markProductAsArchived(String productId, OzonProductOnlineDO product, String clientId) {
        if (product == null) {
            return;
        }
        
        // 标记为已归档和更新状态
        OzonProductOnlineSaveReqVO updateReq = BeanUtils.toBean(product, OzonProductOnlineSaveReqVO.class);
        updateReq.setIsArchived(true);
        updateReq.setStatus(OnlineProductStatusEnum.ARCHIVED.getCode());
        
        log.info("标记商品为已归档: clientId={}, productId={}, id={}", 
                clientId, productId, product.getId());
        
        productOnlineService.updateOzonProductOnline(updateReq);
    }
    
    /**
     * 处理批量归档的商品
     * 
     * @param batchProductIds 批次的商品ID列表
     * @param productIdToRecord 商品ID到记录的映射
     * @param shopMappingDO 店铺映射信息
     */
    private void handleBatchArchivedProducts(List<String> batchProductIds, Map<String, OzonProductOnlineDO> productIdToRecord, 
                                            OzonShopMappingDO shopMappingDO) {
        List<String> localExistProductIds = batchProductIds.stream()
                .filter(productIdToRecord::containsKey)
                .collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(localExistProductIds)) {
            return;
        }
        
        log.info("批次查询失败，将标记 {} 个本地存在的商品为已归档", localExistProductIds.size());
        
        for (String productId : localExistProductIds) {
            markProductAsArchived(productId, productIdToRecord.get(productId), shopMappingDO.getClientId());
        }
    }
    
    /**
     * 清理重复的platformSkuId记录
     * 
     * @param allProductOnlineList 所有商品记录
     * @param clientId 客户端ID
     */
    private void cleanDuplicatePlatformSkuIdRecords(List<OzonProductOnlineDO> allProductOnlineList, String clientId) {
        if (CollectionUtils.isEmpty(allProductOnlineList)) {
            return;
        }
        
        log.info("开始清理客户端 {} 的重复platformSkuId记录", clientId);
        
        // 按platformSkuId分组
        Map<String, List<OzonProductOnlineDO>> groupBySkuId = allProductOnlineList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getPlatformSkuId()))
                .collect(Collectors.groupingBy(OzonProductOnlineDO::getPlatformSkuId));
        
        // 找出重复的记录
        for (Map.Entry<String, List<OzonProductOnlineDO>> entry : groupBySkuId.entrySet()) {
            List<OzonProductOnlineDO> records = entry.getValue();
            if (records.size() > 1) {
                // 保留最新的一条记录，其余标记为删除
                records.sort(Comparator.comparing(OzonProductOnlineDO::getUpdateTime).reversed());
                
                for (int i = 1; i < records.size(); i++) {
                    OzonProductOnlineDO duplicateRecord = records.get(i);
                    log.info("标记重复记录为已删除: clientId={}, platformSkuId={}, id={}", 
                            clientId, entry.getKey(), duplicateRecord.getId());
                    
                    // 调用服务将记录标记为已删除
                    productOnlineService.deleteOzonProductOnline(duplicateRecord.getId());
                }
            }
        }
        
        log.info("客户端 {} 重复platformSkuId记录清理完成", clientId);
    }
    
    /**
     * 批量处理商品在线信息
     * 
     * @param onlineDTOList 在线商品信息列表
     * @param shopMappingDO 店铺映射信息
     */
    private void processProductOnlineItems(List<ProductOnlineDTO> onlineDTOList, OzonShopMappingDO shopMappingDO) {
        if (CollectionUtils.isEmpty(onlineDTOList)) {
            return;
        }
        
        for (ProductOnlineDTO onlineDTO : onlineDTOList) {
            OzonProductOnlineDO productOnline = new OzonProductOnlineDO();

            String platformSkuId = String.valueOf(onlineDTO.getSources().get(0).getSku());

            productOnline.setClientId(shopMappingDO.getClientId());
            productOnline.setOfferId(onlineDTO.getOfferId());
            productOnline.setPlatformSkuId(platformSkuId);
            productOnline.setProductId(onlineDTO.getId().toString());
            productOnline.setIsKgt(onlineDTO.getIsKgt());
            productOnline.setPrice(new BigDecimal(onlineDTO.getPrice()));
            productOnline.setCreateAt(LocalDateTimeUtil.ofUTC(onlineDTO.getCreatedAt().toInstant()));
            productOnline.setTenantId(shopMappingDO.getTenantId());

            // 设置图片，优先使用primaryImage，如果没有则使用images
            String image = null;
            if (CollectionUtils.isNotEmpty(onlineDTO.getPrimaryImage())) {
                image = onlineDTO.getPrimaryImage().get(0);
            } else if (CollectionUtils.isNotEmpty(onlineDTO.getImages())) {
                image = onlineDTO.getImages().get(0);
            }
            productOnline.setImage(image);

            // 设置归档状态
            if (onlineDTO.getIsArchived() || onlineDTO.getIsAutoArchived()) {
                productOnline.setIsArchived(true);
                productOnline.setStatus(OnlineProductStatusEnum.ARCHIVED.getCode());
            } else {
                // 设置状态
                Integer status = OnlineProductStatusEnum.get(onlineDTO.getStatuses().getStatusName()) != null
                        ? OnlineProductStatusEnum.get(onlineDTO.getStatuses().getStatusName()).getCode()
                        : OnlineProductStatusEnum.READY.getCode();
                productOnline.setStatus(status);
            }

            // 设置营销价格
            productOnline.setMarketingPrice(StringUtils.isBlank(onlineDTO.getMarketingPrice()) 
                    ? new BigDecimal(onlineDTO.getPrice()) 
                    : new BigDecimal(onlineDTO.getMarketingPrice()));


            OzonProductOnlineDO existOnlineDO = productOnlineService.getOzonProductOnlineBySkuId(shopMappingDO.getClientId(), platformSkuId);
            if (null == existOnlineDO) {
                productOnlineService.createOzonProductOnline(BeanUtils.toBean(productOnline, OzonProductOnlineSaveReqVO.class));
            } else {
                productOnline.setId(existOnlineDO.getId());
                productOnline.setTenantId(shopMappingDO.getTenantId());
                productOnlineService.updateOzonProductOnline(BeanUtils.toBean(productOnline, OzonProductOnlineSaveReqVO.class));
            }
        }
    }

    private ProductOnlineResponse getOnlineProductInfo2(SyncProductOnlineRequest request) {
        return ozonHttpUtil.postDirect(OzonConfig.OZON_PRODUCT_INFO_API, request, new TypeReference<ProductOnlineResponse>() {
        });
    }

    private OzonHttpResponse<ProductResultDTO> getOnlineProducts(SyncProductOnlineRequest request) {
        TypeReference<OzonHttpResponse<ProductResultDTO>> typeReference = new TypeReference<OzonHttpResponse<ProductResultDTO>>() {
        };

        return ozonHttpUtil.post(OzonConfig.OZON_PRODUCT_LIST_API, request, typeReference);
    }
}
