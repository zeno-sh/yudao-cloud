package cn.iocoder.yudao.module.dm.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.finance.vo.*;

/**
 * 付款单 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface DmFinancePaymentMapper extends BaseMapperX<FinancePaymentDO> {

    default PageResult<FinancePaymentDO> selectPage(FinancePaymentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FinancePaymentDO>()
                .eqIfPresent(FinancePaymentDO::getNo, reqVO.getNo())
                .eqIfPresent(FinancePaymentDO::getAuditStatus, reqVO.getAuditStatus())
                .eqIfPresent(FinancePaymentDO::getOwner, reqVO.getOwner())
                .betweenIfPresent(FinancePaymentDO::getCreateTime, reqVO.getCreateTime())
                .and(reqVO.getBizNo() != null, wrapper -> {
                    wrapper.apply("EXISTS ( " +
                            "SELECT 1 FROM dm_finance_payment_item  " +
                            "WHERE dm_finance_payment_item.payment_id = dm_finance_payment.id AND dm_finance_payment_item.biz_no = {0}" +
                            ")", reqVO.getBizNo());
                })
                .orderByDesc(FinancePaymentDO::getId));
    }

    default FinancePaymentDO selectByNo(String no) {
        return selectOne(FinancePaymentDO::getNo, no);
    }
}