package cn.iocoder.yudao.module.dm.dal.mysql.transaction;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.transaction.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 交易记录 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface OzonFinanceTransactionMapper extends BaseMapperX<OzonFinanceTransactionDO> {

    default PageResult<OzonFinanceTransactionDO> selectPage(OzonFinanceTransactionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OzonFinanceTransactionDO>()
                .inIfPresent(OzonFinanceTransactionDO::getClientId, reqVO.getClientIds())
                .eqIfPresent(OzonFinanceTransactionDO::getOperationId, reqVO.getOperationId())
                .likeIfPresent(OzonFinanceTransactionDO::getPostingNumber, reqVO.getPostingNumber())
                .eqIfPresent(OzonFinanceTransactionDO::getOperationType, reqVO.getOperationType())
                .betweenIfPresent(OzonFinanceTransactionDO::getOperationDate, reqVO.getOperationDate())
                .eqIfPresent(OzonFinanceTransactionDO::getType, reqVO.getType())
                .eqIfPresent(OzonFinanceTransactionDO::getServices, reqVO.getServices())
                .orderByDesc(OzonFinanceTransactionDO::getOperationDate));
    }

    IPage<OzonServiceTransactionRespVO> selectServicePage(IPage<OzonServiceTransactionRespVO> iPage,
                                                          @Param("reqVO") OzonFinanceTransactionPageReqVO pageReqVO);
}