package cn.iocoder.yudao.module.chrome.job;

import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.service.subscription.SubscriptionService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Chrome订阅到期检查定时任务
 * <p>
 * 功能说明：
 * 1. 检查即将到期的订阅，发送提醒通知
 * 2. 记录已到期的订阅信息
 * 3. 处理自动续费逻辑
 *
 * @author Jax
 */
@Service
@Slf4j
public class ChromeSubscriptionExpireJob {

    @Resource
    private SubscriptionService subscriptionService;

    /**
     * 订阅到期检查任务
     *
     * @param param 可选参数（预留，当前未使用）
     * @return 执行结果描述
     */
    @XxlJob("chromeSubscriptionExpireJob")
    @TenantJob
    public String execute(String param) {
        log.info("[ChromeSubscriptionExpireJob][开始执行订阅到期检查任务]");

        try {
            // 1. 处理即将到期的订阅（提前3天提醒）
            int reminderCount = processExpiringSubscriptions();

            // 2. 处理已到期的订阅
            int expiredCount = processExpiredSubscriptions();

            String result = String.format("订阅到期检查任务执行成功，提醒通知: %d，到期处理: %d",
                    reminderCount, expiredCount);
            log.info("[ChromeSubscriptionExpireJob][{}]", result);

            return result;

        } catch (Exception e) {
            log.error("[ChromeSubscriptionExpireJob][订阅到期检查任务执行失败]", e);
            throw new RuntimeException("订阅到期检查任务执行失败", e);
        }
    }

    /**
     * 处理即将到期的订阅（发送提醒通知）
     */
    private int processExpiringSubscriptions() {
        List<SubscriptionDO> expiringSubscriptions = subscriptionService.getExpiringSubscriptions(3);
        int reminderCount = 0;

        for (SubscriptionDO subscription : expiringSubscriptions) {
            try {
                // 这里可以发送邮件、短信或站内消息提醒用户续费
                // 暂时只记录日志
                log.info("[processExpiringSubscriptions][用户({})订阅({})即将到期，结束时间: {}]",
                        subscription.getUserId(), subscription.getId(), subscription.getEndTime());
                reminderCount++;
            } catch (Exception e) {
                log.error("[processExpiringSubscriptions][用户({})到期提醒发送失败]", subscription.getUserId(), e);
            }
        }

        log.info("[processExpiringSubscriptions][即将到期订阅提醒完成，处理数量: {}]", reminderCount);
        return reminderCount;
    }

    /**
     * 处理已到期的订阅（只记录日志，发送通知）
     */
    private int processExpiredSubscriptions() {
        List<SubscriptionDO> expiredSubscriptions = subscriptionService.getExpiringSubscriptions(0);
        int expiredCount = 0;

        for (SubscriptionDO subscription : expiredSubscriptions) {
            try {
                if (subscription.getEndTime().isBefore(LocalDateTime.now())) {
                    // 记录到期信息，可发送邮件/短信通知用户
                    log.info("[processExpiredSubscriptions][用户({})订阅({})已到期，结束时间: {}]",
                            subscription.getUserId(), subscription.getId(), subscription.getEndTime());
                    expiredCount++;
                }
            } catch (Exception e) {
                log.error("[processExpiredSubscriptions][用户({})订阅到期处理失败]", subscription.getUserId(), e);
            }
        }

        log.info("[processExpiredSubscriptions][到期订阅通知完成，处理数量: {}]", expiredCount);
        return expiredCount;
    }
}
