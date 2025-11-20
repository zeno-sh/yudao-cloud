package cn.iocoder.yudao.module.dm.controller.admin.calculation;

import cn.idev.excel.EasyExcel;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationImportReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationRespVO;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.calculation.ProfitCalculationDO;
import cn.iocoder.yudao.module.dm.excel.ProfitCalculationExcelListener;
import cn.iocoder.yudao.module.dm.service.calculation.ProfitCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 利润预测")
@RestController
@RequestMapping("/dm/profit-calculation")
@Validated
public class ProfitCalculationController {

    @Resource
    private ProfitCalculationService profitCalculationService;

    @PostMapping("/create")
    @Operation(summary = "创建利润预测")
    @PreAuthorize("@ss.hasPermission('multiple.platform:profit-calculation:create')")
    public CommonResult<Long> createProfitCalculation(@Valid @RequestBody ProfitCalculationSaveReqVO createReqVO) {
        return success(profitCalculationService.createProfitCalculation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新利润预测")
    @PreAuthorize("@ss.hasPermission('multiple.platform:profit-calculation:update')")
    public CommonResult<Boolean> updateProfitCalculation(@Valid @RequestBody ProfitCalculationSaveReqVO updateReqVO) {
        profitCalculationService.updateProfitCalculation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除利润预测")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('multiple.platform:profit-calculation:delete')")
    public CommonResult<Boolean> deleteProfitCalculation(@RequestParam("id") Long id) {
        profitCalculationService.deleteProfitCalculation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得利润预测")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('multiple.platform:profit-calculation:query')")
    public CommonResult<ProfitCalculationRespVO> getProfitCalculation(@RequestParam("id") Long id) {
        ProfitCalculationDO profitCalculation = profitCalculationService.getProfitCalculation(id);
        return success(BeanUtils.toBean(profitCalculation, ProfitCalculationRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得利润预测分页")
    @PreAuthorize("@ss.hasPermission('multiple.platform:profit-calculation:query')")
    public CommonResult<PageResult<ProfitCalculationRespVO>> getProfitCalculationPage(@Valid ProfitCalculationPageReqVO pageReqVO) {
        PageResult<ProfitCalculationDO> pageResult = profitCalculationService.getProfitCalculationPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProfitCalculationRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出利润预测 Excel")
    @PreAuthorize("@ss.hasPermission('multiple.platform:profit-calculation:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProfitCalculationExcel(@Valid ProfitCalculationPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProfitCalculationDO> list = profitCalculationService.getProfitCalculationPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "利润预测.xls", "数据", ProfitCalculationRespVO.class,
                        BeanUtils.toBean(list, ProfitCalculationRespVO.class));
    }

    @PostMapping("/import")
    @Operation(summary = "导入利润预测")
    @PreAuthorize("@ss.hasPermission('multiple.platform:profit-calculation:import')")
    public CommonResult<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        ProfitCalculationExcelListener listener = new ProfitCalculationExcelListener();
        EasyExcel.read(file.getInputStream(), ProfitCalculationImportReqVO.class, listener).sheet().doRead();

        List<String> errorMessages = listener.getErrorMessages();
        Map<String, Object> response = new HashMap<>();
        if (errorMessages.isEmpty()) {
            response.put("status", 0);
            response.put("message", "导入成功");
        } else {
            response.put("status", 500);
            response.put("errors", errorMessages);
        }
        return success(response);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入利润预测模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        profitCalculationService.getImportTemplate(response);
    }

}