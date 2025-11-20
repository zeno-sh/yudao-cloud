package cn.iocoder.yudao.module.system.api.currency;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.currency.dto.ExchangeRateRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.currency.ExchangeRateDO;
import cn.iocoder.yudao.module.system.service.currency.ExchangeRateService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 汇率信息 API 实现类
 *
 * @author Jax
 */
@Service
@RestController
public class ExchangeRateApiImpl implements ExchangeRateApi {

    @Resource
    private ExchangeRateService exchangeRateService;

    @Override
    public CommonResult<ExchangeRateRespDTO> getExchangeRate(Integer id) {
        ExchangeRateDO exchangeRate = exchangeRateService.getExchangeRate(id);
        return CommonResult.success(BeanUtils.toBean(exchangeRate, ExchangeRateRespDTO.class));
    }

    @Override
    public CommonResult<ExchangeRateRespDTO> getExchangeRateByBaseCurrency(String baseCurrency) {
        ExchangeRateDO exchangeRate = exchangeRateService.getExchangeRateByBaseCurrency(baseCurrency);
        return CommonResult.success(BeanUtils.toBean(exchangeRate, ExchangeRateRespDTO.class));
    }

    @Override
    public CommonResult<List<ExchangeRateRespDTO>> getExchangeRateList() {
        List<ExchangeRateDO> list = exchangeRateService.getExchangeRateList();
        return CommonResult.success(BeanUtils.toBean(list, ExchangeRateRespDTO.class));
    }

}