package cn.iocoder.yudao.module.dm.infrastructure.wb;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.online.vo.OzonProductOnlineSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ProductOnlineItemDTO;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProductOnlineSyncService;
import cn.iocoder.yudao.module.dm.infrastructure.wb.constant.WbConfig;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Card;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Cursor;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Filter;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Settings;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbProductOnlineRequest;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response.WbProductResponse;
import cn.iocoder.yudao.module.dm.infrastructure.wb.utils.WbHttpUtils;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * @author: Zeno
 * @createTime: 2024/07/13 11:45
 */
@Service("wbProductOnlineSyncService")
@Slf4j
public class WbProductOnlineSyncServiceImpl implements ProductOnlineSyncService {
    @Resource
    private WbHttpUtils wbHttpUtils;
    @Resource
    private OzonProductOnlineService productOnlineService;

    @Override
    public void sync(OzonShopMappingDO shopMappingDO) {
        WbProductResponse response = getOnlineProducts(buildRequest(shopMappingDO));

        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getCards())) {
            log.error("查询不到对应产品信息");
            return;
        }

        saveOnlineProduct(shopMappingDO, response.getCards());
    }

    private void saveOnlineProduct(OzonShopMappingDO shopMappingDO, List<Card> cards) {

        if (CollectionUtils.isEmpty(cards)) {
            return;
        }

        List<OzonProductOnlineDO> allProductOnlineList = productOnlineService.getAllProductOnlineByClientId(shopMappingDO.getClientId());
        List<String> localProductIds = convertList(allProductOnlineList, OzonProductOnlineDO::getProductId);
        List<String> onlineProductIds = convertList(cards, card -> String.valueOf(card.getNmID()));

        Sets.SetView<String> difference = Sets.difference(new HashSet<>(localProductIds), new HashSet<>(onlineProductIds));
        for (Card card : cards) {
            String offerId = card.getVendorCode();
            OzonProductOnlineDO productOnline = new OzonProductOnlineDO();
            productOnline.setClientId(shopMappingDO.getClientId());
            productOnline.setOfferId(offerId);
            productOnline.setPlatformSkuId(card.getSizes().get(0).getSkus().get(0));
            productOnline.setProductId(String.valueOf(card.getNmID()));

            if (CollectionUtils.isNotEmpty(card.getPhotos())) {
                productOnline.setImage(card.getPhotos().get(0).getSquare());
            }

            productOnline.setCreateAt(LocalDateTimeUtil.ofUTC(card.getCreatedAt().toInstant()));
            productOnline.setTenantId(shopMappingDO.getTenantId());
            OzonProductOnlineDO existOnlineDO = productOnlineService.getOzonProductOnlineByOfferId(shopMappingDO.getClientId(), offerId);
            if (null == existOnlineDO) {
                productOnlineService.createOzonProductOnline(BeanUtils.toBean(productOnline, OzonProductOnlineSaveReqVO.class));
            } else {
                productOnline.setId(existOnlineDO.getId());
                productOnline.setTenantId(shopMappingDO.getTenantId());
                productOnlineService.updateOzonProductOnline(BeanUtils.toBean(productOnline, OzonProductOnlineSaveReqVO.class));
            }
        }
    }

    private WbProductOnlineRequest buildRequest(OzonShopMappingDO shopMappingDO) {
        WbProductOnlineRequest request = new WbProductOnlineRequest();
        request.setClientId(shopMappingDO.getClientId());
        request.setToken(shopMappingDO.getApiKey());

        Settings settings = new Settings();

        Cursor cursor = new Cursor();
        cursor.setLimit(100L);
        settings.setCursor(cursor);

        Filter filter = new Filter();
        filter.setWithPhoto(-1L);

        settings.setCursor(cursor);
        settings.setFilter(filter);

        request.setSettings(settings);
        return request;
    }


    public WbProductResponse getOnlineProducts(WbProductOnlineRequest request) {
        TypeReference<WbProductResponse> typeReference = new TypeReference<WbProductResponse>() {
        };

        return wbHttpUtils.post(WbConfig.WB_PRODUCT_LIST_API, request, typeReference);
    }
}
