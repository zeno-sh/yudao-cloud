package cn.iocoder.yudao.module.chrome.dal.mysql.referral;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.referral.CommissionRecordDO;
import org.apache.ibatis.annotations.Mapper;
import java.math.BigDecimal;

/**
 * 推广佣金记录 Mapper
 *
 * @author Jax
 */
@Mapper
public interface CommissionRecordMapper extends BaseMapperX<CommissionRecordDO> {

    @org.apache.ibatis.annotations.Select("SELECT SUM(commission_amount) FROM chrome_commission_record WHERE referrer_user_id = #{referrerUserId} AND status != 30 AND deleted = 0")
    BigDecimal selectSumAmountByReferrer(@org.apache.ibatis.annotations.Param("referrerUserId") Long referrerUserId);

}
