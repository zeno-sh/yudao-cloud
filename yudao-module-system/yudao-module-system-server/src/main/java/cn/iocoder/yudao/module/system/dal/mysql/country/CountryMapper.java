package cn.iocoder.yudao.module.system.dal.mysql.country;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.country.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.country.CountryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 国家信息 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface CountryMapper extends BaseMapperX<CountryDO> {

    default PageResult<CountryDO> selectPage(CountryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CountryDO>()
                .eqIfPresent(CountryDO::getCountry, reqVO.getCountry())
                .eqIfPresent(CountryDO::getRegion, reqVO.getRegion())
                .likeIfPresent(CountryDO::getRegionName, reqVO.getRegionName())
                .eqIfPresent(CountryDO::getCurrencyCode, reqVO.getCurrencyCode())
                .betweenIfPresent(CountryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CountryDO::getId));
    }

    /**
     * 根据国家名称获得国家信息
     *
     * @param country 国家名称
     * @return 国家信息
     */
    default CountryDO selectByCountry(String country) {
        return selectOne(CountryDO::getCountry, country);
    }

    /**
     * 根据地区获得国家信息列表
     *
     * @param region 地区
     * @return 国家信息列表
     */
    default List<CountryDO> selectListByRegion(String region) {
        return selectList(CountryDO::getRegion, region);
    }

    /**
     * 根据币种获得国家信息列表
     *
     * @param currencyCode 币种
     * @return 国家信息列表
     */
    default List<CountryDO> selectListByCurrency(String currencyCode) {
        return selectList(CountryDO::getCurrencyCode, currencyCode);
    }

}