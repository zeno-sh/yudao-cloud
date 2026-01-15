package cn.iocoder.yudao.module.chrome.service.subscription;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.chrome.controller.admin.subscription.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.subscription.SubscriptionMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link SubscriptionServiceImpl} 的单元测试类
 *
 * @author Jax
 */
@Import(SubscriptionServiceImpl.class)
public class SubscriptionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SubscriptionServiceImpl subscriptionService;

    @Resource
    private SubscriptionMapper subscriptionMapper;

    @Test
    public void testCreateSubscription_success() {
        // 准备参数
        SubscriptionSaveReqVO createReqVO = randomPojo(SubscriptionSaveReqVO.class).setId(null);

        // 调用
        Long subscriptionId = subscriptionService.createSubscription(createReqVO);
        // 断言
        assertNotNull(subscriptionId);
        // 校验记录的属性是否正确
        SubscriptionDO subscription = subscriptionMapper.selectById(subscriptionId);
        assertPojoEquals(createReqVO, subscription, "id");
    }

    @Test
    public void testUpdateSubscription_success() {
        // mock 数据
        SubscriptionDO dbSubscription = randomPojo(SubscriptionDO.class);
        subscriptionMapper.insert(dbSubscription);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SubscriptionSaveReqVO updateReqVO = randomPojo(SubscriptionSaveReqVO.class, o -> {
            o.setId(dbSubscription.getId()); // 设置更新的 ID
        });

        // 调用
        subscriptionService.updateSubscription(updateReqVO);
        // 校验是否更新正确
        SubscriptionDO subscription = subscriptionMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, subscription);
    }

    @Test
    public void testUpdateSubscription_notExists() {
        // 准备参数
        SubscriptionSaveReqVO updateReqVO = randomPojo(SubscriptionSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> subscriptionService.updateSubscription(updateReqVO), SUBSCRIPTION_NOT_EXISTS);
    }

    @Test
    public void testDeleteSubscription_success() {
        // mock 数据
        SubscriptionDO dbSubscription = randomPojo(SubscriptionDO.class);
        subscriptionMapper.insert(dbSubscription);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSubscription.getId();

        // 调用
        subscriptionService.deleteSubscription(id);
       // 校验数据不存在了
       assertNull(subscriptionMapper.selectById(id));
    }

    @Test
    public void testDeleteSubscription_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> subscriptionService.deleteSubscription(id), SUBSCRIPTION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSubscriptionPage() {
       // mock 数据
       SubscriptionDO dbSubscription = randomPojo(SubscriptionDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setSubscriptionType(null);
           o.setStatus(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setPaymentDuration(null);
           o.setAutoRenew(null);
           o.setCreateTime(null);
       });
       subscriptionMapper.insert(dbSubscription);
       // 测试 userId 不匹配
       subscriptionMapper.insert(cloneIgnoreId(dbSubscription, o -> o.setUserId(null)));
       // 测试 subscriptionType 不匹配
       subscriptionMapper.insert(cloneIgnoreId(dbSubscription, o -> o.setSubscriptionType(null)));
       // 测试 status 不匹配
       subscriptionMapper.insert(cloneIgnoreId(dbSubscription, o -> o.setStatus(null)));
       // 测试 startTime 不匹配
       subscriptionMapper.insert(cloneIgnoreId(dbSubscription, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       subscriptionMapper.insert(cloneIgnoreId(dbSubscription, o -> o.setEndTime(null)));
       // 测试 paymentDuration 不匹配
       subscriptionMapper.insert(cloneIgnoreId(dbSubscription, o -> o.setPaymentDuration(null)));
       // 测试 autoRenew 不匹配
       subscriptionMapper.insert(cloneIgnoreId(dbSubscription, o -> o.setAutoRenew(null)));
       // 测试 createTime 不匹配
       subscriptionMapper.insert(cloneIgnoreId(dbSubscription, o -> o.setCreateTime(null)));
       // 准备参数
       SubscriptionPageReqVO reqVO = new SubscriptionPageReqVO();
       reqVO.setUserId(null);
       reqVO.setSubscriptionType(null);
       reqVO.setStatus(null);
       reqVO.setStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPaymentDuration(null);
       reqVO.setAutoRenew(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<SubscriptionDO> pageResult = subscriptionService.getSubscriptionPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSubscription, pageResult.getList().get(0));
    }

}