package cn.iocoder.yudao.module.dm.infrastructure.wb.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.OrderManagerService;
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
public class WbOrderSyncJob  {

    @Resource
    private OzonShopMappingService dmShopMappingService;
    @Resource
    private OrderManagerService orderManagerService;

    @XxlJob("wbOrderSyncJob")
    @TenantIgnore
    public String execute(String param) throws Exception {
        log.info("[WbOrderSyncJob]-开始同步 WB 订单");

        DateTime now = DateTime.now();
        DateTime utcNow = DateUtil.offsetHour(now, -8);
        DateTime utcYesterday = DateUtil.offsetDay(utcNow, -1);

        String beginDate = DateUtil.format(utcYesterday, DatePattern.UTC_PATTERN);
        String endDate = DateUtil.format(utcNow, DatePattern.UTC_PATTERN);

        if (StringUtils.isNotBlank(param)) {
            // 入参：2024-02-11,2024-02-12 需要转换成 yyyy-MM-dd'T'HH:mm:ss'Z' 格式
            String[] dateTimes = param.split(",");
            beginDate = DateUtil.format(DateUtil.parseDate(dateTimes[0]), DatePattern.UTC_PATTERN);
            endDate = DateUtil.format(DateUtil.endOfDay(DateUtil.parseDate(dateTimes[1])), DatePattern.UTC_PATTERN);
        }

        List<OzonShopMappingDO> shopList = dmShopMappingService.getOzonShopList();
        if (CollectionUtils.isEmpty(shopList)) {
            log.warn("[WbOrderSyncJob]-暂无店铺");
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
            orderManagerService.syncOrder(ozonShopMappingDO.getClientId(), beginDate, endDate);
        }
        log.info("[WbOrderSyncJob]-同步成功");
        return "订单同步完成";
    }
}
