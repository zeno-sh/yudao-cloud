package cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.consumer.order;

import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.OrderManagerService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.IncludeDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FbsOrderInfoRequest;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.OZON_SHOP_MAPPING_NOT_EXISTS;

/**
 * @Author zeno
 * @Date 2024/3/3
 */
@Component
public class BaseListener {

    @Resource
    private OzonShopMappingService dmShopMappingService;
    @Resource
    private OrderManagerService orderManagerService;

    public OzonShopMappingDO queryByClientId(String clientId) {
        OzonShopMappingDO shopMappingDO = dmShopMappingService.getOzonShopMappingByClientId(clientId);
        if (null == shopMappingDO) {
            throw exception(OZON_SHOP_MAPPING_NOT_EXISTS);
        }
        return shopMappingDO;
    }

    public void doFbs(OzonShopMappingDO dmShopMapping, String postingNumber) {
        FbsOrderInfoRequest fbsOrderInfoRequest = new FbsOrderInfoRequest();
        fbsOrderInfoRequest.setApiKey(dmShopMapping.getApiKey());
        fbsOrderInfoRequest.setClientId(dmShopMapping.getClientId());
        fbsOrderInfoRequest.setPostingNumber(postingNumber);

        IncludeDTO includeDTO = new IncludeDTO();
        includeDTO.setBarcodes(true);

        fbsOrderInfoRequest.setInclude(includeDTO);
        orderManagerService.saveOrderByInfo(dmShopMapping.getTenantId(), fbsOrderInfoRequest);
    }
}
