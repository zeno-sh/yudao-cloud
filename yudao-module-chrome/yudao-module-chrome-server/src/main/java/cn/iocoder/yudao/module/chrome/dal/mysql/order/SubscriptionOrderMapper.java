package cn.iocoder.yudao.module.chrome.dal.mysql.order;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.chrome.controller.admin.order.vo.*;

/**
 * 订阅订单 Mapper
 *
 * @author Jax
 */
@Mapper
public interface SubscriptionOrderMapper extends BaseMapperX<SubscriptionOrderDO> {

    default PageResult<SubscriptionOrderDO> selectPage(SubscriptionOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionOrderDO>()
                .eqIfPresent(SubscriptionOrderDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(SubscriptionOrderDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SubscriptionOrderDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(SubscriptionOrderDO::getSubscriptionType, reqVO.getSubscriptionType())
                .eqIfPresent(SubscriptionOrderDO::getBillingCycle, reqVO.getBillingCycle())
                .eqIfPresent(SubscriptionOrderDO::getCredits, reqVO.getCredits())
                .eqIfPresent(SubscriptionOrderDO::getOriginalPrice, reqVO.getOriginalPrice())
                .eqIfPresent(SubscriptionOrderDO::getActualPrice, reqVO.getActualPrice())
                .eqIfPresent(SubscriptionOrderDO::getCurrency, reqVO.getCurrency())
                .eqIfPresent(SubscriptionOrderDO::getPaymentMethod, reqVO.getPaymentMethod())
                .eqIfPresent(SubscriptionOrderDO::getPaymentStatus, reqVO.getPaymentStatus())
                .betweenIfPresent(SubscriptionOrderDO::getPaymentTime, reqVO.getPaymentTime())
                .betweenIfPresent(SubscriptionOrderDO::getExpireTime, reqVO.getExpireTime())
                .betweenIfPresent(SubscriptionOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SubscriptionOrderDO::getId));
    }

    default SubscriptionOrderDO selectByOrderNo(String orderNo) {
        return selectOne(SubscriptionOrderDO::getOrderNo, orderNo);
    }

    /**
     * 统计指定日期的订单数和金额
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @return Map包含订单数和金额
     */
    @org.apache.ibatis.annotations.Select("SELECT " +
            "COUNT(*) as orderCount, " +
            "COALESCE(SUM(actual_price), 0) as totalAmount, " +
            "COUNT(CASE WHEN payment_status = 20 THEN 1 END) as paidOrderCount, " +
            "COALESCE(SUM(CASE WHEN payment_status = 20 THEN actual_price ELSE 0 END), 0) as paidAmount " +
            "FROM chrome_subscription_order " +
            "WHERE DATE(create_time) = #{date} AND deleted = 0")
    Map<String, Object> statisticsOrderByDate(@org.apache.ibatis.annotations.Param("date") String date);

    /**
     * 统计支付状态分布
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @return 支付状态分布列表
     */
    @org.apache.ibatis.annotations.Select("SELECT payment_status, COUNT(*) as count, SUM(actual_price) as amount " +
            "FROM chrome_subscription_order " +
            "WHERE DATE(create_time) = #{date} AND deleted = 0 " +
            "GROUP BY payment_status")
    List<Map<String, Object>> countOrderByPaymentStatus(@org.apache.ibatis.annotations.Param("date") String date);

    /**
     * 统计套餐类型分布
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @return 套餐类型分布列表
     */
    @org.apache.ibatis.annotations.Select("SELECT subscription_type, COUNT(*) as count, SUM(credits) as totalCredits " +
            "FROM chrome_subscription_order " +
            "WHERE DATE(create_time) = #{date} AND deleted = 0 " +
            "GROUP BY subscription_type")
    List<Map<String, Object>> countOrderBySubscriptionType(@org.apache.ibatis.annotations.Param("date") String date);

    /**
     * 统计近N天每日订单趋势
     *
     * @param days 天数
     * @return 每日订单统计列表
     */
    @org.apache.ibatis.annotations.Select("SELECT " +
            "DATE(create_time) as date, " +
            "COUNT(*) as orderCount, " +
            "SUM(actual_price) as totalAmount " +
            "FROM chrome_subscription_order " +
            "WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) AND deleted = 0 " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> countDailyOrders(@org.apache.ibatis.annotations.Param("days") int days);

}