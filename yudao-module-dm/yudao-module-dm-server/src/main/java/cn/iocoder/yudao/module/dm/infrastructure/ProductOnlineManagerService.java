package cn.iocoder.yudao.module.dm.infrastructure;

import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProductOnlineSyncService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author zeno
 * @Date 2024/2/8
 */
@Service
@Slf4j
public class ProductOnlineManagerService {

    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private ProductOnlineSyncService ozonProductOnlineSyncService;
    @Resource
    private ProductOnlineSyncService wbProductOnlineSyncService;

    public void syncOnlineProduct(String clientId) {
        OzonShopMappingDO dmShopMapping = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        Integer platform = dmShopMapping.getPlatform();

        if (DmPlatformEnum.OZON.getPlatformId().equals(platform) || DmPlatformEnum.OZON_GLOBAL.getPlatformId().equals(platform)) {
            ozonProductOnlineSyncService.sync(dmShopMapping);
        } else if (DmPlatformEnum.WB.getPlatformId().equals(platform) || DmPlatformEnum.WB_GLOBAL.getPlatformId().equals(platform)) {
            wbProductOnlineSyncService.sync(dmShopMapping);
        }
    }

}
