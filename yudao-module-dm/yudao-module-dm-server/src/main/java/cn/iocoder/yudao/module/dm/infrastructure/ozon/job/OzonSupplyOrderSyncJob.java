package cn.iocoder.yudao.module.dm.infrastructure.ozon.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.SupplyOrderManagerService;
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
 * Ozon 供应订单同步 Job
 *
 * @author Zeno
 */
@Service
@Slf4j
public class OzonSupplyOrderSyncJob   {

    @Resource
    private OzonShopMappingService dmShopMappingService;
    @Resource
    private SupplyOrderManagerService supplyOrderManagerService;

    @XxlJob("ozonSupplyOrderSyncJob")
    @TenantIgnore
    public String execute(String param) throws Exception {
        log.info("[OzonSupplyOrderSyncJob]-开始同步Ozon供应订单");

        List<OzonShopMappingDO> shopList = dmShopMappingService.getOzonShopList();
        if (CollectionUtils.isEmpty(shopList)) {
            log.warn("[OzonSupplyOrderSyncJob]-暂无店铺");
            return "暂无店铺";
        }

        List<OzonShopMappingDO> ozonShopList = shopList.stream()
                .filter(shop -> shop.getPlatform().equals(DmPlatformEnum.OZON.getPlatformId())
                        || shop.getPlatform().equals(DmPlatformEnum.OZON_GLOBAL.getPlatformId()))
                .collect(Collectors.toList());

        for (OzonShopMappingDO ozonShopMappingDO : ozonShopList) {
            if (StringUtils.isBlank(ozonShopMappingDO.getClientId()) || StringUtils.isBlank(ozonShopMappingDO.getApiKey())) {
                log.warn("账户没有配置,shopId={}", ozonShopMappingDO.getClientId());
                continue;
            }
            try {
                supplyOrderManagerService.syncSupplyOrders(ozonShopMappingDO.getClientId(), ozonShopMappingDO.getApiKey());
            } catch (Exception e) {
                log.error("[OzonSupplyOrderSyncJob]-同步供应订单异常,shopId={}", ozonShopMappingDO.getClientId(), e);
            }
        }

        log.info("[OzonSupplyOrderSyncJob]-同步成功");
        return "供应订单同步完成";
    }
} 