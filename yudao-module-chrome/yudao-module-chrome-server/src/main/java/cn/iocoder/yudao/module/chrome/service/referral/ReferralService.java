package cn.iocoder.yudao.module.chrome.service.referral;

import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;

/**
 * 推广分销 Service 接口
 *
 * @author Jax
 */
public interface ReferralService {

    /**
     * 为用户申请/生成推广码
     * 
     * @param userId 用户ID
     * @return 推广码
     */
    String generateReferralCode(Long userId);

    /**
     * 获取我的推广码
     * 
     * @param userId 用户ID
     * @return 推广码
     */
    String getReferralCode(Long userId);

    /**
     * 处理支付成功后的推广逻辑（异步）
     * 包含：
     * 1. 给被推广者发放奖励（如首单送时长）
     * 2. 给推广者计算并记录佣金
     * 
     * @param order 支付成功的订单
     */
    void processPaySuccessAsync(SubscriptionOrderDO order);

    /**
     * 获取用户的推广信息 (code, 邀请人数, 佣金)
     *
     * @param userId 用户 ID
     * @return 推广信息
     */
    cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo.ChromeReferralInfoRespVO getReferralInfo(Long userId);

    /**
     * 分页查询佣金记录
     *
     * @param reqVO  查询条件
     * @param userId 用户 ID
     * @return 佣金记录分页
     */
    cn.iocoder.yudao.framework.common.pojo.PageResult<cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo.ChromeReferralRecordRespVO> getReferralRecordPage(
            cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo.ChromeReferralRecordPageReqVO reqVO,
            Long userId);

}
