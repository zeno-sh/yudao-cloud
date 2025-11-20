package cn.iocoder.yudao.module.system.controller.admin.country;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.controller.admin.country.vo.CountryPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.country.vo.CountryRespVO;
import cn.iocoder.yudao.module.system.controller.admin.country.vo.CountrySaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.country.CountryDO;
import cn.iocoder.yudao.module.system.service.country.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 国家信息")
@RestController
@RequestMapping("/system/country")
@Validated
public class CountryController {

    @Resource
    private CountryService countryService;

    @PostMapping("/create")
    @Operation(summary = "创建国家信息")
    @PreAuthorize("@ss.hasPermission('system:country:create')")
    public CommonResult<Integer> createCountry(@Valid @RequestBody CountrySaveReqVO createReqVO) {
        return success(countryService.createCountry(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新国家信息")
    @PreAuthorize("@ss.hasPermission('system:country:update')")
    public CommonResult<Boolean> updateCountry(@Valid @RequestBody CountrySaveReqVO updateReqVO) {
        countryService.updateCountry(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除国家信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:country:delete')")
    public CommonResult<Boolean> deleteCountry(@RequestParam("id") Integer id) {
        countryService.deleteCountry(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得国家信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:country:query')")
    public CommonResult<CountryRespVO> getCountry(@RequestParam("id") Integer id) {
        CountryDO country = countryService.getCountry(id);
        return success(BeanUtils.toBean(country, CountryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得国家信息分页")
    @PreAuthorize("@ss.hasPermission('system:country:query')")
    public CommonResult<PageResult<CountryRespVO>> getCountryPage(@Valid CountryPageReqVO pageReqVO) {
        PageResult<CountryDO> pageResult = countryService.getCountryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CountryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得国家信息列表")
    @PreAuthorize("@ss.hasPermission('system:country:query')")
    public CommonResult<List<CountryRespVO>> getCountryList() {
        List<CountryDO> list = countryService.getCountryList();
        return success(BeanUtils.toBean(list, CountryRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出国家信息 Excel")
    @PreAuthorize("@ss.hasPermission('system:country:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCountryExcel(@Valid CountryPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CountryDO> list = countryService.getCountryPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "国家信息.xls", "数据", CountryRespVO.class,
                        BeanUtils.toBean(list, CountryRespVO.class));
    }

}