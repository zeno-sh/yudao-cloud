package cn.iocoder.yudao.module.system.dal.mysql.currency;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.currency.ExchangeRateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 汇率信息 Mapper
 *
 * @author Jax
 */
@Mapper
public interface ExchangeRateMapper extends BaseMapperX<ExchangeRateDO> {

    /**
     * 根据基础货币代码查询汇率信息
     *
     * @param baseCurrency 基础货币代码
     * @return 汇率信息
     */
    default ExchangeRateDO selectByBaseCurrency(String baseCurrency) {
        return selectOne("base_currency", baseCurrency);
    }

    /**
     * 查询所有汇率信息列表
     *
     * @return 汇率信息列表
     */
    default List<ExchangeRateDO> selectList() {
        return selectList("deleted", false);
    }

}