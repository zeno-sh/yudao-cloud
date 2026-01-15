package cn.iocoder.yudao.module.chrome.service.user;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.chrome.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.chrome.dal.mysql.user.ChromeUserMapper;
import cn.iocoder.yudao.module.chrome.service.subscription.SubscriptionService;
import cn.iocoder.yudao.module.chrome.service.credits.UserCreditsService;
import cn.iocoder.yudao.module.chrome.service.usage.UsageRecordService;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.credits.UserCreditsDO;
import cn.iocoder.yudao.module.chrome.enums.SubscriptionTypeEnum;
import cn.iocoder.yudao.module.chrome.controller.plugin.user.vo.UserInfoRespVO;
import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * 用户 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
public class UserServiceImpl implements UserService {

    @Resource
    private ChromeUserMapper chromeUserMapper;
    
    @Resource
    private SubscriptionService subscriptionService;
    
    @Resource
    private UserCreditsService userCreditsService;
    
    @Resource
    private UsageRecordService usageRecordService;

    @Override
    public Long createUser(UserSaveReqVO createReqVO) {
        // 插入
        UserDO user = BeanUtils.toBean(createReqVO, UserDO.class);
        chromeUserMapper.insert(user);
        // 返回
        return user.getId();
    }

    @Override
    public void updateUser(UserSaveReqVO updateReqVO) {
        // 校验存在
        validateUserExists(updateReqVO.getId());
        // 更新
        UserDO updateObj = BeanUtils.toBean(updateReqVO, UserDO.class);
        chromeUserMapper.updateById(updateObj);
    }

    @Override
    public void deleteUser(Long id) {
        // 校验存在
        validateUserExists(id);
        
        // 作废用户的订阅和积分
        subscriptionService.invalidateUserSubscriptions(id);
        userCreditsService.invalidateUserCredits(id);
        
        // 删除用户
        chromeUserMapper.deleteById(id);
    }

    private void validateUserExists(Long id) {
        if (chromeUserMapper.selectById(id) == null) {
            throw exception(USER_NOT_EXISTS);
        }
    }

    @Override
    public UserDO getUser(Long id) {
        return chromeUserMapper.selectById(id);
    }

    @Override
    public PageResult<UserDO> getUserPage(UserPageReqVO pageReqVO) {
        return chromeUserMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateUser(UserDO updateObj) {
        chromeUserMapper.updateById(updateObj);
    }

    @Override
    public UserDO getUserByEmail(String email) {
        return chromeUserMapper.selectByEmail(email);
    }

    @Override
    public void updateUserPassword(Long id, String password) {
        // 校验用户存在
        validateUserExists(id);
        // 更新密码
        UserDO updateObj = new UserDO();
        updateObj.setId(id);
        updateObj.setPassword(password);
        chromeUserMapper.updateById(updateObj);
    }

    @Override
    public UserInfoRespVO getUserInfo(Long id) {
        // 获取用户基本信息
        UserDO user = chromeUserMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        UserInfoRespVO result = BeanUtils.toBean(user, UserInfoRespVO.class);

        // 获取订阅信息
        SubscriptionDO subscription = subscriptionService.getActiveSubscriptionByUserId(id);
        if (subscription != null) {
            result.setSubscriptionType(subscription.getSubscriptionType());
            SubscriptionTypeEnum typeEnum = SubscriptionTypeEnum.valueOf(subscription.getSubscriptionType());
            result.setSubscriptionTypeName(typeEnum != null ? typeEnum.getName() : "未知");
            result.setSubscriptionStatus(subscription.getStatus());
            result.setStartTime(subscription.getStartTime());
            result.setEndTime(subscription.getEndTime());
            result.setBillingCycle(subscription.getBillingCycle());
        } else {
            // 默认免费版
            result.setSubscriptionType(SubscriptionTypeEnum.FREE.getCode());
            result.setSubscriptionTypeName(SubscriptionTypeEnum.FREE.getName());
            result.setSubscriptionStatus(false);
        }

        // 获取积分信息
        UserCreditsDO credits = userCreditsService.getUserCreditsByUserId(id);
        if (credits != null) {
            result.setTotalCredits(credits.getTotalCredits());
            result.setUsedCredits(credits.getUsedCredits());
            result.setRemainingCredits(credits.getRemainingCredits());
            result.setLastResetTime(credits.getLastResetTime());
        } else {
            result.setTotalCredits(0);
            result.setUsedCredits(0);
            result.setRemainingCredits(0);
        }

        // 获取使用统计
        // 今日使用次数（所有功能类型）
        result.setTodayUsageCount(usageRecordService.getTodayUsageCount(id, null));
        
        // 本月使用次数（所有功能类型）
        result.setMonthUsageCount(usageRecordService.getMonthUsageCount(id, null));
        
        // 总使用次数（所有功能类型）
        result.setTotalUsageCount(usageRecordService.getTotalUsageCount(id, null));

        return result;
    }

    @Override
    public UserInfoRespVO getUserInfoByEmail(String email) {
        UserDO user = chromeUserMapper.selectByEmail(email);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        return getUserInfo(user.getId());
    }

    @Override
    public boolean isDeviceTokenExists(String deviceToken) {
        return chromeUserMapper.existsByDeviceToken(deviceToken);
    }

}