package cn.iocoder.yudao.module.dm.infrastructure.ozon.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.FinanceManagerService;
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
 * @Date 2024/2/11
 */
@Service
@Slf4j
public class OzonFinanceSyncJob   {

    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private FinanceManagerService financeManagerService;

    @XxlJob("ozonFinanceSyncJob")
    @TenantIgnore
    public String execute(String param) throws Exception {
        log.info("[OzonFinanceSyncJob]-开始同步Ozon订单");
        DateTime todayDateTime = DateUtil.parseDate(DateUtil.today());
        DateTime tomorrowDateTime = DateUtil.offsetDay(todayDateTime, 1);
        String beginDate = DateUtil.format(todayDateTime, DatePattern.UTC_PATTERN);
        String endDate = DateUtil.format(tomorrowDateTime, DatePattern.UTC_PATTERN);

        List<OzonShopMappingDO> shopList = ozonShopMappingService.getOzonShopList();
        if (CollectionUtils.isEmpty(shopList)) {
            log.warn("[OzonFinanceSyncJob]-暂无店铺");
            return "暂无店铺";
        }

        List<OzonShopMappingDO> ozonShopList = shopList.stream().filter(shop -> shop.getPlatform().equals(DmPlatformEnum.OZON.getPlatformId())
                        || shop.getPlatform().equals(DmPlatformEnum.OZON_GLOBAL.getPlatformId()))
                .collect(Collectors.toList());

        for (OzonShopMappingDO shopMapping : ozonShopList) {
            if (StringUtils.isBlank(shopMapping.getClientId()) || StringUtils.isBlank(shopMapping.getApiKey())) {
                log.warn("账户没有配置,shopId={}", shopMapping.getClientId());
                continue;
            }
            financeManagerService.doSync(shopMapping.getClientId(), beginDate, endDate);
        }

        return "同步成功";
    }
}
