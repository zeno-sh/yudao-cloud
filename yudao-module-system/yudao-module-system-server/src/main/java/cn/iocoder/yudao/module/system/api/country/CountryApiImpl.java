package cn.iocoder.yudao.module.system.api.country;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.country.dto.CountryRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.country.CountryDO;
import cn.iocoder.yudao.module.system.service.country.CountryService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 国家信息 API 实现类
 *
 * @author Zeno
 */
@Service
@RestController
public class CountryApiImpl implements CountryApi {

    @Resource
    private CountryService countryService;

    @Override
    public CommonResult<CountryRespDTO> getCountry(Integer id) {
        CountryDO country = countryService.getCountry(id);
        return CommonResult.success(BeanUtils.toBean(country, CountryRespDTO.class));
    }

    @Override
    public CommonResult<List<CountryRespDTO>> getCountryList() {
        List<CountryDO> list = countryService.getCountryList();
        return CommonResult.success(BeanUtils.toBean(list, CountryRespDTO.class));
    }

    @Override
    public CommonResult<CountryRespDTO> getCountryByName(String country) {
        CountryDO countryDO = countryService.getCountryByName(country);
        return CommonResult.success(BeanUtils.toBean(countryDO, CountryRespDTO.class));
    }

    @Override
    public CommonResult<List<CountryRespDTO>> getCountryListByRegion(String region) {
        List<CountryDO> list = countryService.getCountryListByRegion(region);
        return CommonResult.success(BeanUtils.toBean(list, CountryRespDTO.class));
    }

    @Override
    public CommonResult<List<CountryRespDTO>> getCountryListByCurrency(String currencyCode) {
        List<CountryDO> list = countryService.getCountryListByCurrency(currencyCode);
        return CommonResult.success(BeanUtils.toBean(list, CountryRespDTO.class));
    }

} 