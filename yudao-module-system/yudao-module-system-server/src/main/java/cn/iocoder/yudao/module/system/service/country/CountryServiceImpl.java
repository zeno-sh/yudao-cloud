package cn.iocoder.yudao.module.system.service.country;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.country.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.country.CountryDO;
import cn.iocoder.yudao.module.system.dal.mysql.country.CountryMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 国家信息 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class CountryServiceImpl implements CountryService {

    @Resource
    private CountryMapper countryMapper;

    @Override
    public Integer createCountry(CountrySaveReqVO createReqVO) {
        // 插入
        CountryDO country = BeanUtils.toBean(createReqVO, CountryDO.class);
        countryMapper.insert(country);
        // 返回
        return country.getId();
    }

    @Override
    public void updateCountry(CountrySaveReqVO updateReqVO) {
        // 校验存在
        validateCountryExists(updateReqVO.getId());
        // 更新
        CountryDO updateObj = BeanUtils.toBean(updateReqVO, CountryDO.class);
        countryMapper.updateById(updateObj);
    }

    @Override
    public void deleteCountry(Integer id) {
        // 校验存在
        validateCountryExists(id);
        // 删除
        countryMapper.deleteById(id);
    }

    private void validateCountryExists(Integer id) {
        if (countryMapper.selectById(id) == null) {
            throw exception(COUNTRY_NOT_EXISTS);
        }
    }

    @Override
    public CountryDO getCountry(Integer id) {
        return countryMapper.selectById(id);
    }

    @Override
    public PageResult<CountryDO> getCountryPage(CountryPageReqVO pageReqVO) {
        return countryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CountryDO> getCountryList() {
        return countryMapper.selectList();
    }

    @Override
    public CountryDO getCountryByName(String country) {
        return countryMapper.selectByCountry(country);
    }

    @Override
    public List<CountryDO> getCountryListByRegion(String region) {
        return countryMapper.selectListByRegion(region);
    }

    @Override
    public List<CountryDO> getCountryListByCurrency(String currencyCode) {
        return countryMapper.selectListByCurrency(currencyCode);
    }

}