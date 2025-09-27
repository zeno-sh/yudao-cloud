package cn.iocoder.yudao.module.dm.infrastructure.ozon.job;

import cn.hutool.core.date.DatePattern;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.OzonAdTaskCreationService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Ozon广告同步任务创建Job
 * 负责定期创建Ozon广告同步任务
 *
 * @Author zeno
 * @Date 2024/2/11
 */
@Component
@Slf4j
public class OzonAdSyncJob  {

    @Resource
    private OzonAdTaskCreationService ozonAdTaskCreationService;

    @XxlJob("ozonAdSyncJob")
    @TenantIgnore
    public String execute(String param) throws Exception {
        log.info("[OzonAdSyncJob] 开始创建Ozon广告同步任务，param={}", param);
        
        try {
            // 解析日期参数，默认为莫斯科当天和次天
            String beginDateStr = DmDateUtils.getMoscowToday();
            String endDateStr = DmDateUtils.getMoscowTomorrow();
            if (StringUtils.isNotBlank(param)) {
                String[] dateTimes = param.split(",");
                beginDateStr = dateTimes[0];
                if (dateTimes.length > 1) {
                    endDateStr = dateTimes[1];
                }
            }

            LocalDate beginDate = LocalDate.parse(beginDateStr, DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
            LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));

            // 使用新的任务创建服务
            OzonAdTaskCreationService.TaskCreationResult result = ozonAdTaskCreationService.createTasksForAllShops(
                    beginDate, endDate, "定时任务创建");
            
            log.info("[OzonAdSyncJob] {}", result.getMessage());
            return result.getMessage();
            
        } catch (Exception e) {
            log.error("[OzonAdSyncJob] 执行失败，param={}, error={}", param, e.getMessage(), e);
            throw e;
        }
    }


}
