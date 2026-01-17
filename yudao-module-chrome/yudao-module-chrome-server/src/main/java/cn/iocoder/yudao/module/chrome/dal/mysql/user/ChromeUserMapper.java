package cn.iocoder.yudao.module.chrome.dal.mysql.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.chrome.controller.admin.user.vo.*;

/**
 * 用户 Mapper
 *
 * @author Jax
 */
@Mapper
public interface ChromeUserMapper extends BaseMapperX<UserDO> {

    default PageResult<UserDO> selectPage(UserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserDO>()
                .eqIfPresent(UserDO::getEmail, reqVO.getEmail())
                .eqIfPresent(UserDO::getPassword, reqVO.getPassword())
                .likeIfPresent(UserDO::getNickname, reqVO.getNickname())
                .eqIfPresent(UserDO::getStatus, reqVO.getStatus())
                .eqIfPresent(UserDO::getLoginIp, reqVO.getLoginIp())
                .betweenIfPresent(UserDO::getLoginDate, reqVO.getLoginDate())
                .eqIfPresent(UserDO::getDeviceToken, reqVO.getDeviceToken())
                .betweenIfPresent(UserDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UserDO::getId));
    }

    default UserDO selectByEmail(String email) {
        return selectOne(UserDO::getEmail, email);
    }

    default UserDO selectByDeviceToken(String deviceToken) {
        return selectOne(UserDO::getDeviceToken, deviceToken);
    }

    /**
     * 检查设备令牌是否存在（避免selectOne在多条记录时抛异常）
     *
     * @param deviceToken 设备令牌
     * @return 是否存在
     */
    default boolean existsByDeviceToken(String deviceToken) {
        return selectCount(UserDO::getDeviceToken, deviceToken) > 0;
    }

    /**
     * 统计指定日期的活跃用户数
     * 活跃用户定义：最近7天有积分消耗的用户
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @return 活跃用户数
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(DISTINCT u.id) FROM chrome_user u " +
            "WHERE EXISTS ( " +
            "  SELECT 1 FROM chrome_usage_record ur " +
            "  WHERE ur.user_id = u.id " +
            "  AND ur.usage_date >= DATE_SUB(#{date}, INTERVAL 7 DAY) " +
            "  AND ur.usage_date <= #{date} " +
            "  AND ur.credits_consumed > 0 " +
            "  AND ur.deleted = 0 " +
            ") AND u.deleted = 0")
    int countActiveUsersByDate(@org.apache.ibatis.annotations.Param("date") String date);

    /**
     * 统计指定日期的新增用户数
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @return 新增用户数
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM chrome_user " +
            "WHERE DATE(create_time) = #{date} AND deleted = 0")
    int countNewUsersByDate(@org.apache.ibatis.annotations.Param("date") String date);

    /**
     * 统计总用户数
     *
     * @return 总用户数
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM chrome_user WHERE deleted = 0")
    int countTotalUsers();

    /**
     * 统计日期范围内的新增用户数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 新增用户数
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM chrome_user " +
            "WHERE DATE(create_time) >= #{startDate} AND DATE(create_time) <= #{endDate} AND deleted = 0")
    int countNewUsersByDateRange(@org.apache.ibatis.annotations.Param("startDate") String startDate,
            @org.apache.ibatis.annotations.Param("endDate") String endDate);

    /**
     * 统计近N天每日新增用户数
     *
     * @param days 天数
     * @return 每日新增用户数列表
     */
    @org.apache.ibatis.annotations.Select("SELECT DATE(create_time) as date, COUNT(*) as count " +
            "FROM chrome_user " +
            "WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) AND deleted = 0 " +
            "GROUP BY DATE(create_time) ORDER BY date")
    java.util.List<java.util.Map<String, Object>> countDailyNewUsers(
            @org.apache.ibatis.annotations.Param("days") int days);

    default Long selectCountByReferrer(Long referrerUserId) {
        return selectCount(UserDO::getReferrerUserId, referrerUserId);
    }

}