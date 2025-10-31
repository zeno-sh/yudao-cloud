package cn.iocoder.yudao.module.dm.dal.mysql.exchangerates;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.exchangerates.vo.*;

/**
 * 汇率 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ExchangeRatesMapper extends BaseMapperX<ExchangeRatesDO> {

    default PageResult<ExchangeRatesDO> selectPage(ExchangeRatesPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ExchangeRatesDO>()
                .eqIfPresent(ExchangeRatesDO::getBaseCurrency, reqVO.getBaseCurrency())
                .eqIfPresent(ExchangeRatesDO::getTargetCurrency, reqVO.getTargetCurrency())
                .eqIfPresent(ExchangeRatesDO::getOfficialRate, reqVO.getOfficialRate())
                .eqIfPresent(ExchangeRatesDO::getCustomRate, reqVO.getCustomRate())
                .betweenIfPresent(ExchangeRatesDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ExchangeRatesDO::getId));
    }

}