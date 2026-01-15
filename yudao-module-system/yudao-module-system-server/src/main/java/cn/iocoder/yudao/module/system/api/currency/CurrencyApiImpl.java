package cn.iocoder.yudao.module.system.api.currency;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.currency.dto.CurrencyRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.currency.CurrencyDO;
import cn.iocoder.yudao.module.system.service.currency.CurrencyService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 币种信息 API 实现类
 *
 * @author Jax
 */
@Service
@RestController
public class CurrencyApiImpl implements CurrencyApi {

    @Resource
    private CurrencyService currencyService;

    @Override
    public CommonResult<CurrencyRespDTO> getCurrency(Integer id) {
        CurrencyDO currency = currencyService.getCurrency(id);
        return CommonResult.success(BeanUtils.toBean(currency, CurrencyRespDTO.class));
    }

    @Override
    public CommonResult<CurrencyRespDTO> getCurrencyByCode(String currencyCode) {
        CurrencyDO currency = currencyService.getCurrencyByCode(currencyCode);
        return CommonResult.success(BeanUtils.toBean(currency, CurrencyRespDTO.class));
    }

    @Override
    public CommonResult<List<CurrencyRespDTO>> getCurrencyList() {
        List<CurrencyDO> list = currencyService.getCurrencyList();
        return CommonResult.success(BeanUtils.toBean(list, CurrencyRespDTO.class));
    }

    @Override
    public CommonResult<List<CurrencyRespDTO>> getEnabledCurrencyList() {
        List<CurrencyDO> list = currencyService.getEnabledCurrencyList();
        return CommonResult.success(BeanUtils.toBean(list, CurrencyRespDTO.class));
    }

}