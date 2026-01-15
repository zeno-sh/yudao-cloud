package cn.iocoder.yudao.module.chrome.dal.mysql.transaction;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.transaction.CreditsTransactionDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo.*;

/**
 * 积分交易记录 Mapper
 *
 * @author Jax
 */
@Mapper
public interface CreditsTransactionMapper extends BaseMapperX<CreditsTransactionDO> {

    default PageResult<CreditsTransactionDO> selectPage(CreditsTransactionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CreditsTransactionDO>()
                .eqIfPresent(CreditsTransactionDO::getUserId, reqVO.getUserId())
                .eqIfPresent(CreditsTransactionDO::getTransactionType, reqVO.getTransactionType())
                .eqIfPresent(CreditsTransactionDO::getCreditsAmount, reqVO.getCreditsAmount())
                .eqIfPresent(CreditsTransactionDO::getBeforeCredits, reqVO.getBeforeCredits())
                .eqIfPresent(CreditsTransactionDO::getAfterCredits, reqVO.getAfterCredits())
                .eqIfPresent(CreditsTransactionDO::getBusinessType, reqVO.getBusinessType())
                .eqIfPresent(CreditsTransactionDO::getBusinessId, reqVO.getBusinessId())
                .eqIfPresent(CreditsTransactionDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(CreditsTransactionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CreditsTransactionDO::getId));
    }

    /**
     * 统计指定日期和交易类型的积分总量
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @param transactionType 交易类型
     * @return 积分总量
     */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(credits_amount), 0) FROM chrome_credits_transaction " +
            "WHERE DATE(create_time) = #{date} AND transaction_type = #{transactionType} AND deleted = 0")
    int sumCreditsByDateAndType(@org.apache.ibatis.annotations.Param("date") String date, 
                                @org.apache.ibatis.annotations.Param("transactionType") Integer transactionType);

    /**
     * 统计指定日期和交易类型的记录数
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @param transactionType 交易类型
     * @return 记录数
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM chrome_credits_transaction " +
            "WHERE DATE(create_time) = #{date} AND transaction_type = #{transactionType} AND deleted = 0")
    int countByDateAndType(@org.apache.ibatis.annotations.Param("date") String date, 
                          @org.apache.ibatis.annotations.Param("transactionType") Integer transactionType);

    /**
     * 统计近N天每日充值和消费趋势
     *
     * @param days 天数
     * @return 每日充值消费统计列表
     */
    @org.apache.ibatis.annotations.Select("SELECT " +
            "DATE(create_time) as date, " +
            "SUM(CASE WHEN transaction_type = 10 THEN credits_amount ELSE 0 END) as rechargeCredits, " +
            "SUM(CASE WHEN transaction_type = 20 THEN credits_amount ELSE 0 END) as consumeCredits " +
            "FROM chrome_credits_transaction " +
            "WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) AND deleted = 0 " +
            "GROUP BY DATE(create_time) ORDER BY date")
    java.util.List<java.util.Map<String, Object>> countDailyCreditsFlow(@org.apache.ibatis.annotations.Param("days") int days);

    /**
     * 统计TOP用户积分消耗排行
     *
     * @param date 日期（YYYY-MM-DD格式）
     * @param limit 限制数量
     * @return TOP用户列表
     */
    @org.apache.ibatis.annotations.Select("SELECT " +
            "user_id, " +
            "SUM(credits_amount) as totalCredits " +
            "FROM chrome_credits_transaction " +
            "WHERE DATE(create_time) = #{date} AND transaction_type = 20 AND deleted = 0 " +
            "GROUP BY user_id " +
            "ORDER BY totalCredits DESC " +
            "LIMIT #{limit}")
    java.util.List<java.util.Map<String, Object>> getTopConsumerUsers(@org.apache.ibatis.annotations.Param("date") String date,
                                                                       @org.apache.ibatis.annotations.Param("limit") int limit);

}