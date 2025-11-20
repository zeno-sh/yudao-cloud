package cn.iocoder.yudao.module.dm.excel;

import cn.iocoder.yudao.framework.excel.core.function.ExcelColumnSelectFunction;
import cn.iocoder.yudao.module.system.api.country.CountryApi;
import cn.iocoder.yudao.module.system.api.country.dto.CountryRespDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 国家选择功能类
 *
 * @author Zeno
 */
@Component
public class DemoCountrySelectFunction implements ExcelColumnSelectFunction {

    @Resource
    private CountryApi countryApi;

    @Override
    public String getName() {
        return "demoCountrySelectFunction";
    }

    @Override
    public List<String> getOptions() {
        List<CountryRespDTO> countries = countryApi.getCountryList().getCheckedData();
        return countries.stream()
                .map(CountryRespDTO::getCountry)
                .collect(Collectors.toList());
    }
} 