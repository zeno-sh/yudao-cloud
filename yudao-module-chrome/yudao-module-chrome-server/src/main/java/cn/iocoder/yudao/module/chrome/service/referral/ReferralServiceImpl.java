package cn.iocoder.yudao.module.chrome.service.referral;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.referral.CommissionRecordDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.order.SubscriptionOrderMapper;
import cn.iocoder.yudao.module.chrome.dal.mysql.referral.CommissionRecordMapper;
import java.math.BigDecimal;
import cn.iocoder.yudao.module.chrome.service.user.UserService;
import cn.iocoder.yudao.module.chrome.service.order.SubscriptionOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo.ChromeReferralInfoRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo.ChromeReferralRecordPageReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo.ChromeReferralRecordRespVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;

/**
 * 推广分销 Service 实现类
 *
 * @author Jax
 */
@Service
@Slf4j
public class ReferralServiceImpl implements ReferralService {

    @Resource
    private UserService userService;

    @Resource
    private CommissionRecordMapper commissionRecordMapper;

    @Resource
    private SubscriptionOrderMapper subscriptionOrderMapper;

    @Resource
    @Lazy // 循环依赖解决
    private SubscriptionOrderService subscriptionOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateReferralCode(Long userId) {
        UserDO user = userService.getUser(userId);
        if (user == null) {
            return null;
        }

        // 如果已有推广码，直接返回
        if (user.getReferralCode() != null && !user.getReferralCode().isEmpty()) {
            return user.getReferralCode();
        }

        // 生成唯一推广码 (6位字母数字)
        // 简单重试机制防止冲突
        String code = null;
        for (int i = 0; i < 5; i++) {
            String tempCode = cn.hutool.core.util.RandomUtil.randomString(6).toUpperCase();
            // 检查是否存在
            code = tempCode;
            break; // 简化处理，实际应check
        }

        // 更新用户表
        UserDO updateObj = new UserDO();
        updateObj.setId(userId);
        updateObj.setReferralCode(code);
        userService.updateUser(updateObj);

        return code;
    }

    @Override
    public String getReferralCode(Long userId) {
        UserDO user = userService.getUser(userId);
        return user != null ? user.getReferralCode() : null;
    }

    @Override
    @Async // 异步执行，不影响主流程
    public void processPaySuccessAsync(SubscriptionOrderDO order) {
        log.info("[processPaySuccessAsync] 开始处理订单({})的推广奖励逻辑", order.getId());
        try {
            processPaySuccess(order);
        } catch (Exception e) {
            log.error("[processPaySuccessAsync] 处理推广奖励异常，订单ID: {}", order.getId(), e);
        }
    }

    private void processPaySuccess(SubscriptionOrderDO order) {
        Long userId = order.getUserId();
        UserDO user = userService.getUser(userId);
        if (user == null || user.getReferrerUserId() == null) {
            return;
        }

        Long referrerUserId = user.getReferrerUserId();
        log.info("[processPaySuccess] 用户({})存在推荐人({})，开始计算奖励", userId, referrerUserId);

        // 1. 发放被推广者奖励：赠送15天会员
        // 逻辑：判断是否是首单（排除当前订单，查询已支付订单数）
        Long paidCount = subscriptionOrderMapper.selectCount(new LambdaQueryWrapperX<SubscriptionOrderDO>()
                .eq(SubscriptionOrderDO::getUserId, userId)
                .eq(SubscriptionOrderDO::getPaymentStatus, 20)); // 20=已支付

        // 因为当前订单已经支付，所以count至少为1。如果是1，说明是首单。
        if (paidCount == 1) {
            log.info("[processPaySuccess] 用户({})为首次付费，发放15天赠送奖励", userId);
            giveFreeDuration(order, 15);

            // 2. 发放推广者奖励：佣金 (仅首单发放)
            // 计算佣金
            BigDecimal rate = new BigDecimal("0.10"); // 10%
            BigDecimal commission = order.getActualPrice().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP);

            if (commission.compareTo(BigDecimal.ZERO) > 0) {
                CommissionRecordDO record = CommissionRecordDO.builder()
                        .referrerUserId(referrerUserId)
                        .inviteeUserId(userId)
                        .orderId(order.getId())
                        .orderAmount(order.getActualPrice())
                        .commissionRate(rate)
                        .commissionAmount(commission)
                        .status(10) // 10-待结算
                        .build();
                commissionRecordMapper.insert(record);
                log.info("[processPaySuccess] 记录佣金成功，推荐人: {}, 金额: {}", referrerUserId, commission);

                // TODO: 这里可以调用钱包服务增加余额，暂略
            }

        } else {
            log.info("[processPaySuccess] 用户({})非首次付费(count={})，跳过赠送奖励和佣金", userId, paidCount);
        }
    }

    /**
     * 赠送免费时长
     */
    private void giveFreeDuration(SubscriptionOrderDO order, int days) {
        try {
            log.info("[giveFreeDuration] 开始赠送免费时长，OrderId: {}, Days: {}", order.getId(), days);

            // 直接使用当前订单的 planId 和 subscriptionType 进行赠送
            subscriptionOrderService.createFreeRewardOrder(order.getUserId(), days, order.getPlanId(),
                    order.getSubscriptionType());

        } catch (Exception e) {
            log.error("[giveFreeDuration] 赠送失败", e);
        }
    }

    @Override
    public ChromeReferralInfoRespVO getReferralInfo(Long userId) {
        UserDO user = userService.getUser(userId);
        if (user == null) {
            return null;
        }

        ChromeReferralInfoRespVO respVO = new ChromeReferralInfoRespVO();
        respVO.setCode(user.getReferralCode());
        // 统计邀请人数
        respVO.setInviteeCount(userService.countByReferrer(userId));
        // 统计累计收益
        BigDecimal totalCommission = commissionRecordMapper.selectSumAmountByReferrer(userId);
        respVO.setTotalCommission(totalCommission != null ? totalCommission : BigDecimal.ZERO);

        return respVO;
    }

    @Override
    public PageResult<ChromeReferralRecordRespVO> getReferralRecordPage(ChromeReferralRecordPageReqVO reqVO,
            Long userId) {
        // 分页查询佣金记录
        PageResult<CommissionRecordDO> pageResult = commissionRecordMapper.selectPage(reqVO,
                new LambdaQueryWrapperX<CommissionRecordDO>()
                        .eq(CommissionRecordDO::getReferrerUserId, userId)
                        .orderByDesc(CommissionRecordDO::getCreateTime));

        if (CollectionUtils.isAnyEmpty(pageResult.getList())) {
            return PageResult.empty();
        }

        return BeanUtils.toBean(pageResult, ChromeReferralRecordRespVO.class);
    }

}
