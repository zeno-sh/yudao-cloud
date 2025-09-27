package cn.iocoder.yudao.module.dm.infrastructure.wb.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.wb.WbOrderStatusSyncService;
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
 * @author: Zeno
 * @createTime: 2024/07/13 11:16
 */
@Service
@Slf4j
public class WbOrderStatusSyncJob {

    @Resource
    private OzonShopMappingService dmShopMappingService;
    @Resource
    private WbOrderStatusSyncService wbOrderStatusSyncService;

    @XxlJob("wbOrderStatusSyncJob")
    @TenantIgnore
    public String execute(String param) throws Exception {
        log.info("[WbOrderStatusSyncJob]-开始同步Ozon订单状态");
        DateTime todayDateTime = DateUtil.parseDate(DateUtil.today());
        DateTime tomorrowDateTime = DateUtil.offsetDay(todayDateTime, 1);

        List<OzonShopMappingDO> shopList = dmShopMappingService.getOzonShopList();
        if (CollectionUtils.isEmpty(shopList)) {
            log.warn("[WbOrderStatusSyncJob]-暂无店铺");
            return "暂无店铺";
        }

        List<OzonShopMappingDO> wbShopList = shopList.stream().filter(shop -> shop.getPlatform().equals(DmPlatformEnum.WB.getPlatformId())
                        || shop.getPlatform().equals(DmPlatformEnum.WB_GLOBAL.getPlatformId()))
                .collect(Collectors.toList());

        for (OzonShopMappingDO ozonShopMappingDO : wbShopList) {
            if (StringUtils.isBlank(ozonShopMappingDO.getClientId()) || StringUtils.isBlank(ozonShopMappingDO.getApiKey())) {
                log.warn("账户没有配置,shopId={}", ozonShopMappingDO.getClientId());
                continue;
            }
            wbOrderStatusSyncService.sync(ozonShopMappingDO.getClientId(),
                    DateUtil.today(),
                    DateUtil.formatDate(tomorrowDateTime));
        }
        log.info("[WbOrderStatusSyncJob]-同步成功");
        return "订单状态同步完成";
    }
}
