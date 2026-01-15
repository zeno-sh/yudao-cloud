package cn.iocoder.yudao.module.chrome.service.plan;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.chrome.controller.admin.plan.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 订阅套餐配置 Service 接口
 *
 * @author Jax
 */
public interface SubscriptionPlanService {

    /**
     * 创建订阅套餐配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSubscriptionPlan(@Valid SubscriptionPlanSaveReqVO createReqVO);

    /**
     * 更新订阅套餐配置
     *
     * @param updateReqVO 更新信息
     */
    void updateSubscriptionPlan(@Valid SubscriptionPlanSaveReqVO updateReqVO);

    /**
     * 删除订阅套餐配置
     *
     * @param id 编号
     */
    void deleteSubscriptionPlan(Long id);

    /**
     * 获得订阅套餐配置
     *
     * @param id 编号
     * @return 订阅套餐配置
     */
    SubscriptionPlanDO getSubscriptionPlan(Long id);

    /**
     * 获得订阅套餐配置分页
     *
     * @param pageReqVO 分页查询
     * @return 订阅套餐配置分页
     */
    PageResult<SubscriptionPlanDO> getSubscriptionPlanPage(SubscriptionPlanPageReqVO pageReqVO);

    /**
     * 获得所有订阅套餐配置
     *
     * @return 订阅套餐配置列表
     */
    List<SubscriptionPlanDO> getAllSubscriptionPlans();

    /**
     * 获得所有有效启用的订阅套餐配置
     *
     * @return 有效启用的订阅套餐配置列表
     */
    List<SubscriptionPlanDO> getEnabledSubscriptionPlans();

}