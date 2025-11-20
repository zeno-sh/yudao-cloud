package cn.iocoder.yudao.module.system.controller.admin.currency;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.currency.vo.CurrencyRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.currency.CurrencyDO;
import cn.iocoder.yudao.module.system.service.currency.CurrencyService;
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

@Tag(name = "管理后台 - 币种信息")
@RestController
@RequestMapping("/system/currency")
@Validated
public class CurrencyController {

    @Resource
    private CurrencyService currencyService;

    @GetMapping("/get")
    @Operation(summary = "获得币种信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<CurrencyRespVO> getCurrency(@RequestParam("id") Integer id) {
        CurrencyDO currency = currencyService.getCurrency(id);
        return success(BeanUtils.toBean(currency, CurrencyRespVO.class));
    }

    @GetMapping("/get-by-code")
    @Operation(summary = "根据货币代码获得币种信息")
    @Parameter(name = "currencyCode", description = "货币代码", required = true, example = "USD")
    public CommonResult<CurrencyRespVO> getCurrencyByCode(@RequestParam("currencyCode") String currencyCode) {
        CurrencyDO currency = currencyService.getCurrencyByCode(currencyCode);
        return success(BeanUtils.toBean(currency, CurrencyRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得币种信息列表")
    public CommonResult<List<CurrencyRespVO>> getCurrencyList() {
        List<CurrencyDO> list = currencyService.getCurrencyList();
        return success(BeanUtils.toBean(list, CurrencyRespVO.class));
    }

}