package cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.consumer.ad;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.ReportAdService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.message.ad.AdMessage;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.iocoder.yudao.module.dm.infrastructure.config.OzonAsyncConfiguration.DM_JOB_THREAD_POOL_TASK_EXECUTOR;

/**
 * @author: Zeno
 * @createTime: 2024/11/26 21:17
 */
@Component
@Slf4j
public class AdConsumer {

    @Resource
    private ReportAdService reportAdService;

    @EventListener
    @Async(DM_JOB_THREAD_POOL_TASK_EXECUTOR)
    public void onMessage(AdMessage message) {
        log.info("[onMessage][消息内容({})]", JSON.toJSONString(message));
        reportAdService.doSync(message.getClientId(), message.getBeginDate(), message.getEndDate());
    }
}
