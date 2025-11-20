package cn.iocoder.yudao.module.system.controller.admin.currency;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.currency.vo.ExchangeRateRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.currency.ExchangeRateDO;
import cn.iocoder.yudao.module.system.service.currency.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 汇率信息")
@RestController
@RequestMapping("/system/exchange-rate")
@Validated
public class ExchangeRateController {

    @Resource
    private ExchangeRateService exchangeRateService;

    @GetMapping("/get")
    @Operation(summary = "获得汇率信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<ExchangeRateRespVO> getExchangeRate(@RequestParam("id") Integer id) {
        ExchangeRateDO exchangeRate = exchangeRateService.getExchangeRate(id);
        return success(BeanUtils.toBean(exchangeRate, ExchangeRateRespVO.class));
    }

    @GetMapping("/get-by-currency")
    @Operation(summary = "根据基础货币代码获得汇率信息")
    @Parameter(name = "baseCurrency", description = "基础货币代码", required = true, example = "USD")
    public CommonResult<ExchangeRateRespVO> getExchangeRateByBaseCurrency(@RequestParam("baseCurrency") String baseCurrency) {
        ExchangeRateDO exchangeRate = exchangeRateService.getExchangeRateByBaseCurrency(baseCurrency);
        return success(BeanUtils.toBean(exchangeRate, ExchangeRateRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得汇率信息列表")
    public CommonResult<List<ExchangeRateRespVO>> getExchangeRateList() {
        List<ExchangeRateDO> list = exchangeRateService.getExchangeRateList();
        return success(BeanUtils.toBean(list, ExchangeRateRespVO.class));
    }

}