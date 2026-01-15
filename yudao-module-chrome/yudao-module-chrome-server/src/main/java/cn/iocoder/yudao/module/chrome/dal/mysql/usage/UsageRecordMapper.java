package cn.iocoder.yudao.module.chrome.dal.mysql.usage;

import java.util.*;
import java.time.LocalDateTime;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.usage.UsageRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import cn.iocoder.yudao.module.chrome.controller.admin.usage.vo.*;

/**
 * Chrome使用记录 Mapper
 *
 * @author Jax
 */
@Mapper
public interface UsageRecordMapper extends BaseMapperX<UsageRecordDO> {

    default PageResult<UsageRecordDO> selectPage(UsageRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UsageRecordDO>()
                .eqIfPresent(UsageRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(UsageRecordDO::getFeatureType, reqVO.getFeatureType())
                .betweenIfPresent(UsageRecordDO::getUsageDate, reqVO.getUsageDate())
                .eqIfPresent(UsageRecordDO::getUsageCount, reqVO.getUsageCount())
                .betweenIfPresent(UsageRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UsageRecordDO::getId));
    }

    /**
     * 查询今日使用次数
     *
     * @param userId 用户ID
     * @param featureType 功能类型，为null时查询所有功能类型
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 今日使用次数
     */
    @Select("SELECT COALESCE(SUM(usage_count), 0) FROM chrome_usage_record " +
            "WHERE user_id = #{userId} " +
            "AND (#{featureType} IS NULL OR feature_type = #{featureType}) " +
            "AND create_time >= #{startOfDay} AND create_time < #{endOfDay}")
    int selectTodayUsageCount(@Param("userId") Long userId, 
                             @Param("featureType") Integer featureType,
                             @Param("startOfDay") LocalDateTime startOfDay,
                             @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查询本月使用次数
     *
     * @param userId 用户ID
     * @param featureType 功能类型，为null时查询所有功能类型
     * @param startOfMonth 本月开始时间
     * @param endOfMonth 本月结束时间
     * @return 本月使用次数
     */
    @Select("SELECT COALESCE(SUM(usage_count), 0) FROM chrome_usage_record " +
            "WHERE user_id = #{userId} " +
            "AND (#{featureType} IS NULL OR feature_type = #{featureType}) " +
            "AND create_time >= #{startOfMonth} AND create_time < #{endOfMonth}")
    int selectMonthUsageCount(@Param("userId") Long userId, 
                             @Param("featureType") Integer featureType,
                             @Param("startOfMonth") LocalDateTime startOfMonth,
                             @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * 查询总使用次数
     *
     * @param userId 用户ID
     * @param featureType 功能类型，为null时查询所有功能类型
     * @return 总使用次数
     */
    @Select("SELECT COALESCE(SUM(usage_count), 0) FROM chrome_usage_record " +
            "WHERE user_id = #{userId} " +
            "AND (#{featureType} IS NULL OR feature_type = #{featureType})")
    int selectTotalUsageCount(@Param("userId") Long userId, 
                             @Param("featureType") Integer featureType);

    /**
     * 统计指定日期的总积分消耗
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @return 总积分消耗
     */
    @Select("SELECT COALESCE(SUM(credits_consumed), 0) FROM chrome_usage_record " +
            "WHERE usage_date = #{date} AND deleted = 0")
    int sumCreditsConsumedByDate(@Param("date") String date);

    /**
     * 统计指定日期各功能类型的积分消耗
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @param featureType 功能类型
     * @return 该功能类型的积分消耗
     */
    @Select("SELECT COALESCE(SUM(credits_consumed), 0) FROM chrome_usage_record " +
            "WHERE usage_date = #{date} AND feature_type = #{featureType} AND deleted = 0")
    int sumCreditsConsumedByDateAndFeature(@Param("date") String date, @Param("featureType") Integer featureType);

    /**
     * 统计功能使用排行（按使用次数）
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @return 功能使用排行列表
     */
    @Select("SELECT " +
            "feature_type, " +
            "SUM(usage_count) as usageCount, " +
            "SUM(credits_consumed) as creditsConsumed " +
            "FROM chrome_usage_record " +
            "WHERE usage_date = #{date} AND deleted = 0 " +
            "GROUP BY feature_type " +
            "ORDER BY usageCount DESC")
    java.util.List<java.util.Map<String, Object>> getFeatureUsageRanking(@Param("date") String date);

    /**
     * 统计近N天各功能使用趋势
     *
     * @param days 天数
     * @return 各功能使用趋势列表
     */
    @Select("SELECT " +
            "usage_date as date, " +
            "feature_type, " +
            "SUM(usage_count) as usageCount, " +
            "SUM(credits_consumed) as creditsConsumed " +
            "FROM chrome_usage_record " +
            "WHERE usage_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) AND deleted = 0 " +
            "GROUP BY usage_date, feature_type " +
            "ORDER BY usage_date, feature_type")
    java.util.List<java.util.Map<String, Object>> getFeatureUsageTrend(@Param("days") int days);

    /**
     * 统计指定日期每个用户的积分消耗明细
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @return 用户积分消耗明细列表
     */
    @Select("SELECT " +
            "ur.user_id as userId, " +
            "u.email as email, " +
            "u.nickname as nickname, " +
            "SUM(ur.credits_consumed) as creditsConsumed, " +
            "SUM(ur.usage_count) as usageCount " +
            "FROM chrome_usage_record ur " +
            "LEFT JOIN chrome_user u ON ur.user_id = u.id " +
            "WHERE ur.usage_date = #{date} AND ur.deleted = 0 " +
            "GROUP BY ur.user_id, u.email, u.nickname " +
            "ORDER BY creditsConsumed DESC")
    java.util.List<java.util.Map<String, Object>> getUserCreditsConsumeByDate(@Param("date") String date);

}