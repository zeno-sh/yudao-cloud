package cn.iocoder.yudao.module.system.dal.mysql.currency;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.currency.CurrencyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 币种信息 Mapper
 *
 * @author Jax
 */
@Mapper
public interface CurrencyMapper extends BaseMapperX<CurrencyDO> {

    /**
     * 根据货币代码查询币种信息
     *
     * @param currencyCode 货币代码
     * @return 币种信息
     */
    default CurrencyDO selectByCurrencyCode(String currencyCode) {
        return selectOne("currency_code", currencyCode);
    }

    /**
     * 查询所有币种信息列表
     *
     * @return 币种信息列表
     */
    default List<CurrencyDO> selectList() {
        return selectList("deleted", false);
    }

}