package cn.iocoder.yudao.module.dm.excel;

import cn.iocoder.yudao.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.framework.excel.core.function.ExcelColumnSelectFunction;
import cn.iocoder.yudao.module.system.api.currency.CurrencyApi;
import cn.iocoder.yudao.module.system.api.currency.dto.CurrencyRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 币种选择功能类
 *
 * @author Zeno
 */
@Component
public class DemoCurrencySelectFunction implements ExcelColumnSelectFunction {

    @Resource
    private DictDataApi dictDataApi;

    @Resource
    private CurrencyApi currencyApi;

    @Override
    public String getName() {
        return "demoCurrencySelectFunction";
    }

    @Override
    public List<String> getOptions() {
        List<CurrencyRespDTO> currencyRespDTOList = currencyApi.getCurrencyList().getCheckedData();
        return currencyRespDTOList.stream()
                .map(CurrencyRespDTO::getCurrencyCode)
                .collect(Collectors.toList());
    }

} 