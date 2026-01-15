package cn.iocoder.yudao.module.chrome.job;

import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.chrome.service.credits.UserCreditsService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Chrome积分重置定时任务（暂时不使用）
 *
 * 功能说明：
 * 1. 每月重置月付用户积分为订阅套餐额度
 *
 * @author Jax
 */
@Service
@Slf4j
public class ChromeCreditsResetJob {

    @Resource
    private UserCreditsService userCreditsService;

    /**
     * 重置月付用户积分
     *
     * @param param 可选参数（预留，当前未使用）
     * @return 执行结果描述
     */
    @XxlJob("chromeCreditsResetJob")
    @TenantJob
    public String execute(String param) {
        log.info("[ChromeCreditsResetJob][开始执行积分重置任务]");

        try {
            // 重置月付用户积分
            int monthlyUserResetCount = userCreditsService.batchResetMonthlyUserCredits();
            log.info("[ChromeCreditsResetJob][月付用户积分重置完成，重置用户数: {}]", monthlyUserResetCount);

            String result = String.format("积分重置任务执行成功，月付用户重置: %d", monthlyUserResetCount);
            log.info("[ChromeCreditsResetJob][{}]", result);

            return result;

        } catch (Exception e) {
            log.error("[ChromeCreditsResetJob][积分重置任务执行失败]", e);
            throw new RuntimeException("积分重置任务执行失败", e);
        }
    }
}
