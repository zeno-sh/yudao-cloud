package cn.iocoder.yudao.module.dm.infrastructure.ozon.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.impl.OzonProductOnlineSyncServiceImpl;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author zeno
 * @Date 2024/3/2
 */
@Service
@Slf4j
public class OzonProductSyncJob   {

    @Resource
    private OzonProductOnlineSyncServiceImpl ozonProductOnlineSyncService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;


    @XxlJob("ozonProductSyncJob")
    @TenantIgnore
    public String execute(String param) throws Exception {
        log.info("[OnlineProductSyncJobService]-开始同步Ozon在线产品");
        List<OzonShopMappingDO> shopList = ozonShopMappingService.getOzonShopList();
        if (CollectionUtils.isEmpty(shopList)) {
            log.warn("[OzonProductSyncJob]-暂无店铺");
            return "暂无店铺";
        }

        List<OzonShopMappingDO> ozonShopList = shopList.stream().filter(shop -> shop.getPlatform().equals(DmPlatformEnum.OZON.getPlatformId())
                        || shop.getPlatform().equals(DmPlatformEnum.OZON_GLOBAL.getPlatformId()))
                .collect(Collectors.toList());

        for (OzonShopMappingDO dmShopMapping : ozonShopList) {
            if (StringUtils.isBlank(dmShopMapping.getClientId()) || StringUtils.isBlank(dmShopMapping.getApiKey())) {
                log.warn("账户没有配置,shopId={}", dmShopMapping.getClientId());
                continue;
            }
            ozonProductOnlineSyncService.sync(dmShopMapping);
        }
        return "同步完成";
    }
}
