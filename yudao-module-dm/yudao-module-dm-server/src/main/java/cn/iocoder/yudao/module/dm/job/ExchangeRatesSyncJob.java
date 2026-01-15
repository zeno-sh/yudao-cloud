package cn.iocoder.yudao.module.dm.job;

import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.dm.service.exchangerates.ExchangeRatesService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 汇率同步定时任务
 * 
 * 从外部 API 获取最新汇率，只更新官方汇率，不修改自定义汇率
 *
 * @author Zeno
 */
@Service
@Slf4j
public class ExchangeRatesSyncJob {

    @Resource
    private ExchangeRatesService exchangeRatesService;

    /**
     * 同步当前租户的官方汇率
     *
     * @param param 可选参数（预留，当前未使用）
     * @return 执行结果描述
     */
    @XxlJob("exchangeRatesSyncJob")
    @TenantJob
    public String execute(String param) {
        log.info("[ExchangeRatesSyncJob] 开始同步官方汇率");
        int updated = exchangeRatesService.syncOfficialExchangeRates();
        String result = String.format("汇率同步完成，共更新 %d 条记录", updated);
        log.info("[ExchangeRatesSyncJob] {}", result);
        return result;
    }
}
