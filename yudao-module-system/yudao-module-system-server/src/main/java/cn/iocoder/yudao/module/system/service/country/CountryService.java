package cn.iocoder.yudao.module.system.service.country;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.country.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.country.CountryDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 国家信息 Service 接口
 *
 * @author Zeno
 */
public interface CountryService {

    /**
     * 创建国家信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createCountry(@Valid CountrySaveReqVO createReqVO);

    /**
     * 更新国家信息
     *
     * @param updateReqVO 更新信息
     */
    void updateCountry(@Valid CountrySaveReqVO updateReqVO);

    /**
     * 删除国家信息
     *
     * @param id 编号
     */
    void deleteCountry(Integer id);

    /**
     * 获得国家信息
     *
     * @param id 编号
     * @return 国家信息
     */
    CountryDO getCountry(Integer id);

    /**
     * 获得国家信息分页
     *
     * @param pageReqVO 分页查询
     * @return 国家信息分页
     */
    PageResult<CountryDO> getCountryPage(CountryPageReqVO pageReqVO);

    /**
     * 获得国家信息列表
     *
     * @return 国家信息列表
     */
    List<CountryDO> getCountryList();

    /**
     * 根据国家名称获得国家信息
     *
     * @param country 国家名称
     * @return 国家信息
     */
    CountryDO getCountryByName(String country);

    /**
     * 根据地区获得国家信息列表
     *
     * @param region 地区
     * @return 国家信息列表
     */
    List<CountryDO> getCountryListByRegion(String region);

    /**
     * 根据币种获得国家信息列表
     *
     * @param currencyCode 币种
     * @return 国家信息列表
     */
    List<CountryDO> getCountryListByCurrency(String currencyCode);

}