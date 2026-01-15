package cn.iocoder.yudao.module.chrome.service.plan;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.math.BigDecimal;
import cn.iocoder.yudao.module.chrome.controller.admin.plan.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.chrome.dal.mysql.plan.SubscriptionPlanMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * 订阅套餐配置 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    @Resource
    private SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public Long createSubscriptionPlan(SubscriptionPlanSaveReqVO createReqVO) {
        // 插入
        SubscriptionPlanDO subscriptionPlan = BeanUtils.toBean(createReqVO, SubscriptionPlanDO.class);
        // 计算折扣后价格
        calculateDiscountedPrice(subscriptionPlan);
        subscriptionPlanMapper.insert(subscriptionPlan);
        // 返回
        return subscriptionPlan.getId();
    }

    @Override
    public void updateSubscriptionPlan(SubscriptionPlanSaveReqVO updateReqVO) {
        // 校验存在
        validateSubscriptionPlanExists(updateReqVO.getId());
        // 更新
        SubscriptionPlanDO updateObj = BeanUtils.toBean(updateReqVO, SubscriptionPlanDO.class);
        // 计算折扣后价格
        calculateDiscountedPrice(updateObj);
        subscriptionPlanMapper.updateById(updateObj);
    }

    @Override
    public void deleteSubscriptionPlan(Long id) {
        // 校验存在
        validateSubscriptionPlanExists(id);
        // 删除
        subscriptionPlanMapper.deleteById(id);
    }

    private void validateSubscriptionPlanExists(Long id) {
        if (subscriptionPlanMapper.selectById(id) == null) {
            throw exception(SUBSCRIPTION_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public SubscriptionPlanDO getSubscriptionPlan(Long id) {
        return subscriptionPlanMapper.selectById(id);
    }

    @Override
    public PageResult<SubscriptionPlanDO> getSubscriptionPlanPage(SubscriptionPlanPageReqVO pageReqVO) {
        return subscriptionPlanMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SubscriptionPlanDO> getAllSubscriptionPlans() {
        return subscriptionPlanMapper.selectList();
    }

    @Override
    public List<SubscriptionPlanDO> getEnabledSubscriptionPlans() {
        return subscriptionPlanMapper.selectList("status", true);
    }

    /**
     * 计算折扣后价格
     * @param subscriptionPlan 套餐对象
     */
    private void calculateDiscountedPrice(SubscriptionPlanDO subscriptionPlan) {
        if (subscriptionPlan.getPrice() == null) {
            return;
        }
        
        // 如果没有设置折扣率或折扣率为null，则不设置折扣后价格（使用原价）
        if (subscriptionPlan.getDiscountRate() == null) {
            subscriptionPlan.setDiscountedPrice(null);
            return;
        }
        
        // 验证折扣率范围（0-100%）
        if (subscriptionPlan.getDiscountRate().compareTo(BigDecimal.ZERO) < 0 || 
            subscriptionPlan.getDiscountRate().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("折扣率必须在0-100之间");
        }
        
        // 如果折扣率为0，则折扣后价格等于原价
        if (subscriptionPlan.getDiscountRate().compareTo(BigDecimal.ZERO) == 0) {
            subscriptionPlan.setDiscountedPrice(subscriptionPlan.getPrice());
            return;
        }
        
        // 计算折扣后价格
        BigDecimal discountAmount = subscriptionPlan.getPrice()
            .multiply(subscriptionPlan.getDiscountRate())
            .divide(new BigDecimal("100"), 0, BigDecimal.ROUND_HALF_UP);
        BigDecimal discountedPrice = subscriptionPlan.getPrice().subtract(discountAmount);
        
        // 确保折扣后价格不为负数
        if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
            discountedPrice = BigDecimal.ZERO;
        }
        
        subscriptionPlan.setDiscountedPrice(discountedPrice);
    }

}