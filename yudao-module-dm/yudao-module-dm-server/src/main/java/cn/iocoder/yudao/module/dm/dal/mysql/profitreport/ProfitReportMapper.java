package cn.iocoder.yudao.module.dm.dal.mysql.profitreport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.*;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 财务账单报告 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProfitReportMapper extends BaseMapperX<ProfitReportDO> {

    default PageResult<ProfitReportDO> selectPage(ProfitReportPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProfitReportDO>()
                .betweenIfPresent(ProfitReportDO::getFinanceDate, reqVO.getFinanceDate())
                .inIfPresent(ProfitReportDO::getClientId, reqVO.getClientIds())
                .likeIfPresent(ProfitReportDO::getOfferId, reqVO.getOfferId())
                .orderByDesc(ProfitReportDO::getId));
    }

    IPage<ProfitReportRespVO> selectClientPage(IPage<ProfitReportRespVO> page, @Param("reqVO")  ProfitReportPageReqVO reqVO);

    IPage<ProfitReportRespVO> selectSkuPage(IPage<ProfitReportRespVO> page, @Param("reqVO")  ProfitReportPageReqVO reqVO);

    void deleteProfitReport(@Param("clientIds") String[] clientIds, @Param("financeDate") LocalDate[] financeDate);
}