package cn.iocoder.yudao.module.chrome.service.plan;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.chrome.controller.admin.plan.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.plan.SubscriptionPlanMapper;
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
 * {@link SubscriptionPlanServiceImpl} 的单元测试类
 *
 * @author Jax
 */
@Import(SubscriptionPlanServiceImpl.class)
public class SubscriptionPlanServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SubscriptionPlanServiceImpl subscriptionPlanService;

    @Resource
    private SubscriptionPlanMapper subscriptionPlanMapper;

    @Test
    public void testCreateSubscriptionPlan_success() {
        // 准备参数
        SubscriptionPlanSaveReqVO createReqVO = randomPojo(SubscriptionPlanSaveReqVO.class).setId(null);

        // 调用
        Long subscriptionPlanId = subscriptionPlanService.createSubscriptionPlan(createReqVO);
        // 断言
        assertNotNull(subscriptionPlanId);
        // 校验记录的属性是否正确
        SubscriptionPlanDO subscriptionPlan = subscriptionPlanMapper.selectById(subscriptionPlanId);
        assertPojoEquals(createReqVO, subscriptionPlan, "id");
    }

    @Test
    public void testUpdateSubscriptionPlan_success() {
        // mock 数据
        SubscriptionPlanDO dbSubscriptionPlan = randomPojo(SubscriptionPlanDO.class);
        subscriptionPlanMapper.insert(dbSubscriptionPlan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SubscriptionPlanSaveReqVO updateReqVO = randomPojo(SubscriptionPlanSaveReqVO.class, o -> {
            o.setId(dbSubscriptionPlan.getId()); // 设置更新的 ID
        });

        // 调用
        subscriptionPlanService.updateSubscriptionPlan(updateReqVO);
        // 校验是否更新正确
        SubscriptionPlanDO subscriptionPlan = subscriptionPlanMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, subscriptionPlan);
    }

    @Test
    public void testUpdateSubscriptionPlan_notExists() {
        // 准备参数
        SubscriptionPlanSaveReqVO updateReqVO = randomPojo(SubscriptionPlanSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> subscriptionPlanService.updateSubscriptionPlan(updateReqVO), SUBSCRIPTION_PLAN_NOT_EXISTS);
    }

    @Test
    public void testDeleteSubscriptionPlan_success() {
        // mock 数据
        SubscriptionPlanDO dbSubscriptionPlan = randomPojo(SubscriptionPlanDO.class);
        subscriptionPlanMapper.insert(dbSubscriptionPlan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSubscriptionPlan.getId();

        // 调用
        subscriptionPlanService.deleteSubscriptionPlan(id);
       // 校验数据不存在了
       assertNull(subscriptionPlanMapper.selectById(id));
    }

    @Test
    public void testDeleteSubscriptionPlan_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> subscriptionPlanService.deleteSubscriptionPlan(id), SUBSCRIPTION_PLAN_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSubscriptionPlanPage() {
       // mock 数据
       SubscriptionPlanDO dbSubscriptionPlan = randomPojo(SubscriptionPlanDO.class, o -> { // 等会查询到
           o.setPlanName(null);
           o.setSubscriptionType(null);
           o.setBillingCycle(null);
           o.setCredits(null);
           o.setPrice(null);
           o.setCurrency(null);
           o.setStatus(null);
           o.setSortOrder(null);
           o.setDescription(null);
           o.setCreateTime(null);
       });
       subscriptionPlanMapper.insert(dbSubscriptionPlan);
       // 测试 planName 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setPlanName(null)));
       // 测试 subscriptionType 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setSubscriptionType(null)));
       // 测试 billingCycle 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setBillingCycle(null)));
       // 测试 credits 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setCredits(null)));
       // 测试 price 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setPrice(null)));
       // 测试 currency 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setCurrency(null)));
       // 测试 status 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setStatus(null)));
       // 测试 sortOrder 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setSortOrder(null)));
       // 测试 description 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setDescription(null)));
       // 测试 createTime 不匹配
       subscriptionPlanMapper.insert(cloneIgnoreId(dbSubscriptionPlan, o -> o.setCreateTime(null)));
       // 准备参数
       SubscriptionPlanPageReqVO reqVO = new SubscriptionPlanPageReqVO();
       reqVO.setPlanName(null);
       reqVO.setSubscriptionType(null);
       reqVO.setBillingCycle(null);
       reqVO.setCredits(null);
       reqVO.setPrice(null);
       reqVO.setCurrency(null);
       reqVO.setStatus(null);
       reqVO.setSortOrder(null);
       reqVO.setDescription(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<SubscriptionPlanDO> pageResult = subscriptionPlanService.getSubscriptionPlanPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSubscriptionPlan, pageResult.getList().get(0));
    }

}