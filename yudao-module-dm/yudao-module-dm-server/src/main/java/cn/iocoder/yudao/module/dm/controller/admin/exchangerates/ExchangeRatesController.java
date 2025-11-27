package cn.iocoder.yudao.module.dm.controller.admin.exchangerates;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.dm.controller.admin.exchangerates.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.module.dm.service.exchangerates.ExchangeRatesService;

@Tag(name = "管理后台 - 汇率")
@RestController
@RequestMapping("/dm/exchange-rates")
@Validated
public class ExchangeRatesController {

    @Resource
    private ExchangeRatesService exchangeRatesService;

    @PostMapping("/create")
    @Operation(summary = "创建汇率")
    @PreAuthorize("@ss.hasPermission('dm:exchange-rates:create')")
    public CommonResult<Long> createExchangeRates(@Valid @RequestBody ExchangeRatesSaveReqVO createReqVO) {
        return success(exchangeRatesService.createExchangeRates(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新汇率")
    @PreAuthorize("@ss.hasPermission('dm:exchange-rates:update')")
    public CommonResult<Boolean> updateExchangeRates(@Valid @RequestBody ExchangeRatesSaveReqVO updateReqVO) {
        exchangeRatesService.updateExchangeRates(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除汇率")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:exchange-rates:delete')")
    public CommonResult<Boolean> deleteExchangeRates(@RequestParam("id") Long id) {
        exchangeRatesService.deleteExchangeRates(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得汇率")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:exchange-rates:query')")
    public CommonResult<ExchangeRatesRespVO> getExchangeRates(@RequestParam("id") Long id) {
        ExchangeRatesDO exchangeRates = exchangeRatesService.getExchangeRates(id);
        return success(BeanUtils.toBean(exchangeRates, ExchangeRatesRespVO.class));
    }

    @GetMapping("/get-by-currency-code")
    @Operation(summary = "根据币种代码获得汇率")
    @Parameter(name = "currencyCode", description = "币种代码", required = true, example = "USD")
    @PreAuthorize("@ss.hasPermission('dm:exchange-rates:query')")
    public CommonResult<ExchangeRatesRespVO> getExchangeRatesByCurrencyCode(@RequestParam("currencyCode") String currencyCode) {
        ExchangeRatesDO exchangeRates = exchangeRatesService.getExchangeRatesByCurrencyCode(currencyCode);
        return success(BeanUtils.toBean(exchangeRates, ExchangeRatesRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得汇率分页")
    @PreAuthorize("@ss.hasPermission('dm:exchange-rates:query')")
    public CommonResult<PageResult<ExchangeRatesRespVO>> getExchangeRatesPage(@Valid ExchangeRatesPageReqVO pageReqVO) {
        PageResult<ExchangeRatesDO> pageResult = exchangeRatesService.getExchangeRatesPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ExchangeRatesRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出汇率 Excel")
    @PreAuthorize("@ss.hasPermission('dm:exchange-rates:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExchangeRatesExcel(@Valid ExchangeRatesPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ExchangeRatesDO> list = exchangeRatesService.getExchangeRatesPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "汇率.xls", "数据", ExchangeRatesRespVO.class,
                        BeanUtils.toBean(list, ExchangeRatesRespVO.class));
    }

}