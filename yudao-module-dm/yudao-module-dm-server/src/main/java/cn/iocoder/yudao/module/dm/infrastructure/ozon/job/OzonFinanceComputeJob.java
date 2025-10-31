package cn.iocoder.yudao.module.dm.infrastructure.ozon.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProfitComputeService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 根据账单生成产品利润报表
 *
 * @Author zeno
 * @Date 2024/2/11
 */
@Service
@Slf4j
public class OzonFinanceComputeJob   {

    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private ProfitComputeService ozonProfitComputeService;

    @XxlJob("ozonFinanceComputeJob")
    @TenantIgnore
    public String execute(String param) throws Exception {
        log.info("[OzonFinanceComputeJob]-开始预计算");

        DateTime yesterdayDateTime = DateUtil.yesterday();
        String financeDate = DateUtil.format(yesterdayDateTime, DatePattern.NORM_DATE_PATTERN);
        List<String> ozonShopIds = new ArrayList<>();

        if (StringUtils.isNotBlank(param)) {
            financeDate = param.split(",")[0];
            ozonShopIds.add(param.split(",")[1]);
        } else {
            List<OzonShopMappingDO> shopList = ozonShopMappingService.getOzonShopList();
            if (CollectionUtils.isEmpty(shopList)) {
                log.warn("[OzonFinanceComputeJob]-暂无店铺");
                return "暂无店铺";
            }

            List<OzonShopMappingDO> ozonShopList = shopList.stream()
                    .filter(shop -> shop.getPlatform().equals(DmPlatformEnum.OZON.getPlatformId())
                            || shop.getPlatform().equals(DmPlatformEnum.OZON_GLOBAL.getPlatformId()))
                    .collect(Collectors.toList());


            for (OzonShopMappingDO shopMapping : ozonShopList) {
                if (StringUtils.isBlank(shopMapping.getClientId()) || StringUtils.isBlank(shopMapping.getApiKey())) {
                    log.warn("账户没有配置,shopId={}", shopMapping.getClientId());
                    continue;
                }
                ozonShopIds.add(shopMapping.getClientId());
            }
        }

        ozonProfitComputeService.computeProfitReport(financeDate, ozonShopIds);
        return "生成成功";
    }
}
