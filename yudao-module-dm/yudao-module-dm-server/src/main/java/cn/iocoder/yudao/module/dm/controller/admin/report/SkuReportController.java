package cn.iocoder.yudao.module.dm.controller.admin.report;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.report.vo.SkuReportQueryReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.report.vo.SkuReportRespVO;
import cn.iocoder.yudao.module.dm.service.report.SkuReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * SKU报表 Controller
 *
 * @author Jax
 */
@Tag(name = "管理后台 - SKU报表")
@RestController
@RequestMapping("/dm/sku-report")
@Validated
@Slf4j
public class SkuReportController {

    @Resource
    private SkuReportService skuReportService;

    @PostMapping("/query")
    @Operation(summary = "查询SKU报表")
    @PreAuthorize("@ss.hasPermission('dm:sku-report:query')")
    public CommonResult<PageResult<SkuReportRespVO>> querySkuReport(@Valid @RequestBody SkuReportQueryReqVO reqVO) {
        // 设置默认时间：如果没有传时间，默认查询昨天结束往前7天
        if (reqVO.getStartTime() == null && reqVO.getEndTime() == null) {
            reqVO.setEndTime(LocalDate.now().minusDays(1));
            reqVO.setStartTime(reqVO.getEndTime().minusDays(6));
        }
        
        PageResult<SkuReportRespVO> pageResult = skuReportService.querySkuReport(reqVO);
        return success(pageResult);
    }

    @PostMapping("/export")
    @Operation(summary = "导出SKU报表")
    @PreAuthorize("@ss.hasPermission('dm:sku-report:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSkuReport(@Valid @RequestBody SkuReportQueryReqVO reqVO,
                                HttpServletResponse response) throws IOException {
        // 设置默认时间：如果没有传时间，默认查询昨天结束往前7天
        if (reqVO.getStartTime() == null && reqVO.getEndTime() == null) {
            reqVO.setEndTime(LocalDate.now().minusDays(1));
            reqVO.setStartTime(reqVO.getEndTime().minusDays(6));
        }
        
        // 导出全部数据,不分页
        List<SkuReportRespVO> list = skuReportService.querySkuReportList(reqVO);
        ExcelUtils.write(response, "SKU报表.xlsx", "SKU数据", SkuReportRespVO.class, list);
    }
}
