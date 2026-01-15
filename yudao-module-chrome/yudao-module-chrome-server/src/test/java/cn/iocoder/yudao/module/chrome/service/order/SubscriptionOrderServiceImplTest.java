package cn.iocoder.yudao.module.chrome.service.order;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.chrome.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.order.SubscriptionOrderMapper;
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
 * {@link SubscriptionOrderServiceImpl} 的单元测试类
 *
 * @author Jax
 */
@Import(SubscriptionOrderServiceImpl.class)
public class SubscriptionOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SubscriptionOrderServiceImpl subscriptionOrderService;

    @Resource
    private SubscriptionOrderMapper subscriptionOrderMapper;

    @Test
    public void testCreateSubscriptionOrder_success() {
        // 准备参数
        SubscriptionOrderSaveReqVO createReqVO = randomPojo(SubscriptionOrderSaveReqVO.class).setId(null);

        // 调用
        Long subscriptionOrderId = subscriptionOrderService.createSubscriptionOrder(createReqVO);
        // 断言
        assertNotNull(subscriptionOrderId);
        // 校验记录的属性是否正确
        SubscriptionOrderDO subscriptionOrder = subscriptionOrderMapper.selectById(subscriptionOrderId);
        assertPojoEquals(createReqVO, subscriptionOrder, "id");
    }

    @Test
    public void testUpdateSubscriptionOrder_success() {
        // mock 数据
        SubscriptionOrderDO dbSubscriptionOrder = randomPojo(SubscriptionOrderDO.class);
        subscriptionOrderMapper.insert(dbSubscriptionOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SubscriptionOrderSaveReqVO updateReqVO = randomPojo(SubscriptionOrderSaveReqVO.class, o -> {
            o.setId(dbSubscriptionOrder.getId()); // 设置更新的 ID
        });

        // 调用
        subscriptionOrderService.updateSubscriptionOrder(updateReqVO);
        // 校验是否更新正确
        SubscriptionOrderDO subscriptionOrder = subscriptionOrderMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, subscriptionOrder);
    }

    @Test
    public void testUpdateSubscriptionOrder_notExists() {
        // 准备参数
        SubscriptionOrderSaveReqVO updateReqVO = randomPojo(SubscriptionOrderSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> subscriptionOrderService.updateSubscriptionOrder(updateReqVO), SUBSCRIPTION_ORDER_NOT_EXISTS);
    }

    @Test
    public void testDeleteSubscriptionOrder_success() {
        // mock 数据
        SubscriptionOrderDO dbSubscriptionOrder = randomPojo(SubscriptionOrderDO.class);
        subscriptionOrderMapper.insert(dbSubscriptionOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSubscriptionOrder.getId();

        // 调用
        subscriptionOrderService.deleteSubscriptionOrder(id);
       // 校验数据不存在了
       assertNull(subscriptionOrderMapper.selectById(id));
    }

    @Test
    public void testDeleteSubscriptionOrder_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> subscriptionOrderService.deleteSubscriptionOrder(id), SUBSCRIPTION_ORDER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSubscriptionOrderPage() {
       // mock 数据
       SubscriptionOrderDO dbSubscriptionOrder = randomPojo(SubscriptionOrderDO.class, o -> { // 等会查询到
           o.setOrderNo(null);
           o.setUserId(null);
           o.setPlanId(null);
           o.setSubscriptionType(null);
           o.setBillingCycle(null);
           o.setCredits(null);
           o.setOriginalPrice(null);
           o.setActualPrice(null);
           o.setCurrency(null);
           o.setPaymentMethod(null);
           o.setPaymentStatus(null);
           o.setPaymentTime(null);
           o.setExpireTime(null);
           o.setCreateTime(null);
       });
       subscriptionOrderMapper.insert(dbSubscriptionOrder);
       // 测试 orderNo 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setOrderNo(null)));
       // 测试 userId 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setUserId(null)));
       // 测试 planId 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setPlanId(null)));
       // 测试 subscriptionType 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setSubscriptionType(null)));
       // 测试 billingCycle 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setBillingCycle(null)));
       // 测试 credits 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setCredits(null)));
       // 测试 originalPrice 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setOriginalPrice(null)));
       // 测试 actualPrice 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setActualPrice(null)));
       // 测试 currency 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setCurrency(null)));
       // 测试 paymentMethod 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setPaymentMethod(null)));
       // 测试 paymentStatus 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setPaymentStatus(null)));
       // 测试 paymentTime 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setPaymentTime(null)));
       // 测试 expireTime 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setExpireTime(null)));
       // 测试 createTime 不匹配
       subscriptionOrderMapper.insert(cloneIgnoreId(dbSubscriptionOrder, o -> o.setCreateTime(null)));
       // 准备参数
       SubscriptionOrderPageReqVO reqVO = new SubscriptionOrderPageReqVO();
       reqVO.setOrderNo(null);
       reqVO.setUserId(null);
       reqVO.setPlanId(null);
       reqVO.setSubscriptionType(null);
       reqVO.setBillingCycle(null);
       reqVO.setCredits(null);
       reqVO.setOriginalPrice(null);
       reqVO.setActualPrice(null);
       reqVO.setCurrency(null);
       reqVO.setPaymentMethod(null);
       reqVO.setPaymentStatus(null);
       reqVO.setPaymentTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setExpireTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<SubscriptionOrderDO> pageResult = subscriptionOrderService.getSubscriptionOrderPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSubscriptionOrder, pageResult.getList().get(0));
    }

}