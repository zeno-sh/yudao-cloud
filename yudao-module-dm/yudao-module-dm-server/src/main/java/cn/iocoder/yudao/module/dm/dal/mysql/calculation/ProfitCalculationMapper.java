package cn.iocoder.yudao.module.dm.dal.mysql.calculation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.calculation.ProfitCalculationDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 利润预测 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProfitCalculationMapper extends BaseMapperX<ProfitCalculationDO> {

    default PageResult<ProfitCalculationDO> selectPage(ProfitCalculationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProfitCalculationDO>()
                .eqIfPresent(ProfitCalculationDO::getProductId, reqVO.getProductId())
                .likeIfPresent(ProfitCalculationDO::getPlanName, reqVO.getPlanName())
                .eqIfPresent(ProfitCalculationDO::getPlatform, reqVO.getPlatform())
                .eqIfPresent(ProfitCalculationDO::getCountry, reqVO.getCountry())
                .eqIfPresent(ProfitCalculationDO::getTemplateId, reqVO.getTemplateId())
                .eqIfPresent(ProfitCalculationDO::getPrice, reqVO.getPrice())
                .eqIfPresent(ProfitCalculationDO::getCurrency, reqVO.getCurrency())
                .eqIfPresent(ProfitCalculationDO::getExchangeRate, reqVO.getExchangeRate())
                .eqIfPresent(ProfitCalculationDO::getPurchaseCost, reqVO.getPurchaseCost())
                .geIfPresent(ProfitCalculationDO::getGrossMargin, reqVO.getGrossMarginMin())
                .leIfPresent(ProfitCalculationDO::getGrossMargin, reqVO.getGrossMarginMax())
                .geIfPresent(ProfitCalculationDO::getRoi, reqVO.getRoiMin())
                .leIfPresent(ProfitCalculationDO::getRoi, reqVO.getRoiMax())
                .betweenIfPresent(ProfitCalculationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProfitCalculationDO::getId));
    }

}