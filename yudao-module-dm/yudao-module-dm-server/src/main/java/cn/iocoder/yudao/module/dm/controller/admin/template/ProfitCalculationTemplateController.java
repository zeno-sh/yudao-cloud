package cn.iocoder.yudao.module.dm.controller.admin.template;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.template.vo.ProfitCalculationTemplatePageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.template.vo.ProfitCalculationTemplateRespVO;
import cn.iocoder.yudao.module.dm.controller.admin.template.vo.ProfitCalculationTemplateSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.service.template.ProfitCalculationTemplateService;
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

@Tag(name = "管理后台 - 利润计算配置模板")
@RestController
@RequestMapping("/dm/profit-calculation-template")
@Validated
public class ProfitCalculationTemplateController {

    @Resource
    private ProfitCalculationTemplateService profitCalculationTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建利润计算配置模板")
    @PreAuthorize("@ss.hasPermission('multiple:profit-calculation-template:create')")
    public CommonResult<Long> createProfitCalculationTemplate(@Valid @RequestBody ProfitCalculationTemplateSaveReqVO createReqVO) {
        return success(profitCalculationTemplateService.createProfitCalculationTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新利润计算配置模板")
    @PreAuthorize("@ss.hasPermission('multiple:profit-calculation-template:update')")
    public CommonResult<Boolean> updateProfitCalculationTemplate(@Valid @RequestBody ProfitCalculationTemplateSaveReqVO updateReqVO) {
        profitCalculationTemplateService.updateProfitCalculationTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除利润计算配置模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('multiple:profit-calculation-template:delete')")
    public CommonResult<Boolean> deleteProfitCalculationTemplate(@RequestParam("id") Long id) {
        profitCalculationTemplateService.deleteProfitCalculationTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得利润计算配置模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('multiple:profit-calculation-template:query')")
    public CommonResult<ProfitCalculationTemplateRespVO> getProfitCalculationTemplate(@RequestParam("id") Long id) {
        ProfitCalculationTemplateDO profitCalculationTemplate = profitCalculationTemplateService.getProfitCalculationTemplate(id);
        return success(BeanUtils.toBean(profitCalculationTemplate, ProfitCalculationTemplateRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得利润计算配置模板分页")
    @PreAuthorize("@ss.hasPermission('multiple:profit-calculation-template:query')")
    public CommonResult<PageResult<ProfitCalculationTemplateRespVO>> getProfitCalculationTemplatePage(@Valid ProfitCalculationTemplatePageReqVO pageReqVO) {
        PageResult<ProfitCalculationTemplateDO> pageResult = profitCalculationTemplateService.getProfitCalculationTemplatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProfitCalculationTemplateRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出利润计算配置模板 Excel")
    @PreAuthorize("@ss.hasPermission('multiple:profit-calculation-template:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProfitCalculationTemplateExcel(@Valid ProfitCalculationTemplatePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProfitCalculationTemplateDO> list = profitCalculationTemplateService.getProfitCalculationTemplatePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "利润计算配置模板.xls", "数据", ProfitCalculationTemplateRespVO.class,
                        BeanUtils.toBean(list, ProfitCalculationTemplateRespVO.class));
    }

}