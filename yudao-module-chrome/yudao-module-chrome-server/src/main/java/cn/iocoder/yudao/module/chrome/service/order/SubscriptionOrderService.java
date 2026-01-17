package cn.iocoder.yudao.module.chrome.service.order;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.chrome.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 订阅订单 Service 接口
 *
 * @author Jax
 */
public interface SubscriptionOrderService {

    /**
     * 创建订阅订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSubscriptionOrder(@Valid SubscriptionOrderSaveReqVO createReqVO);

    /**
     * 更新订阅订单
     *
     * @param updateReqVO 更新信息
     */
    void updateSubscriptionOrder(@Valid SubscriptionOrderSaveReqVO updateReqVO);

    /**
     * 删除订阅订单
     *
     * @param id 编号
     */
    void deleteSubscriptionOrder(Long id);

    /**
     * 获得订阅订单
     *
     * @param id 编号
     * @return 订阅订单
     */
    SubscriptionOrderDO getSubscriptionOrder(Long id);

    /**
     * 获得订阅订单分页
     *
     * @param pageReqVO 分页查询
     * @return 订阅订单分页
     */
    PageResult<SubscriptionOrderDO> getSubscriptionOrderPage(SubscriptionOrderPageReqVO pageReqVO);

    /**
     * 获得指定用户的已支付订单分页
     *
     * @param userId    用户ID
     * @param pageReqVO 分页参数
     * @return 订单分页
     */
    PageResult<SubscriptionOrderDO> getPaidOrdersByUserId(Long userId, PageParam pageReqVO);

}