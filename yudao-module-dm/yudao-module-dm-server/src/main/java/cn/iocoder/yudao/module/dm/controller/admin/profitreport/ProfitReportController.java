package cn.iocoder.yudao.module.dm.controller.admin.profitreport;

import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.AuthShopMappingService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.OzonProfitService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.ProfitComputeManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.*;
import javax.servlet.http.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportService;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportTaskLogDO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Tag(name = "管理后台 - 财务账单报告")
@RestController
@RequestMapping("/dm/profit-report")
@Validated
public class ProfitReportController {

    @Resource
    private ProfitReportService profitReportService;
    @Resource
    private AuthShopMappingService authShopMappingService;
    @Resource
    private OzonProfitService ozonProfitService;
    @Resource
    private ProfitComputeManagerService profitComputeManagerService;
    @Resource
    private ProfitReportTaskLogService profitReportTaskLogService;

    @PostMapping("/create")
    @Operation(summary = "创建财务账单报告")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:create')")
    public CommonResult<Integer> createProfitReport(@Valid @RequestBody ProfitReportSaveReqVO createReqVO) {
        return success(profitReportService.createProfitReport(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新财务账单报告")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:update')")
    public CommonResult<Boolean> updateProfitReport(@Valid @RequestBody ProfitReportSaveReqVO updateReqVO) {
        profitReportService.updateProfitReport(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除财务账单报告")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:profit-report:delete')")
    public CommonResult<Boolean> deleteProfitReport(@RequestParam("id") Integer id) {
        profitReportService.deleteProfitReport(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得财务账单报告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:query')")
    public CommonResult<ProfitReportRespVO> getProfitReport(@RequestParam("id") Integer id) {
        ProfitReportDO profitReport = profitReportService.getProfitReport(id);
        return success(BeanUtils.toBean(profitReport, ProfitReportRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得财务账单报告分页")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:query')")
    public CommonResult<PageResult<ProfitReportRespVO>> getProfitReportPage(@Valid ProfitReportPageReqVO pageReqVO) {
        PageResult<ProfitReportDO> pageResult = profitReportService.getProfitReportPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProfitReportRespVO.class));
    }

    @GetMapping("/page-client")
    @Operation(summary = "获得财务账单报告分页")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:query')")
    public CommonResult<PageResult<ProfitReportRespVO>> getClientProfitReportPage(@Valid ProfitReportPageReqVO pageReqVO) {

        if (pageReqVO.getDimension().equals("sku")) {
            return success(PageResult.empty());
        }

        String[] authClientIds = getClientIds(pageReqVO.getClientIds());
        pageReqVO.setClientIds(authClientIds);

        IPage<ProfitReportRespVO> iPageResult = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        profitReportService.getClientProfitReportPage(iPageResult, pageReqVO);


        List<ProfitReportRespVO> records = iPageResult.getRecords();
        ozonProfitService.handleClientProfitReport(pageReqVO, records);
        return success(new PageResult<>(records, iPageResult.getTotal()));
    }

    @GetMapping("/page-sku")
    @Operation(summary = "获得财务账单报告分页")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:query')")
    public CommonResult<PageResult<ProfitReportRespVO>> getSkuProfitReportPage(@Valid ProfitReportPageReqVO pageReqVO) {
        if (pageReqVO.getDimension().equals("client")) {
            return success(PageResult.empty());
        }

        String[] authClientIds = getClientIds(pageReqVO.getClientIds());
        pageReqVO.setClientIds(authClientIds);

        IPage<ProfitReportRespVO> iPageResult = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        profitReportService.getSkuProfitReportPage(iPageResult, pageReqVO);

        List<ProfitReportRespVO> records = iPageResult.getRecords();
        ozonProfitService.handleSkuProfitReport(records);

        return success(new PageResult<>(records, iPageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出财务账单报告 Excel")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProfitReportExcel(@Valid ProfitReportPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        // 获取权限内的门店ID
        String[] authClientIds = getClientIds(pageReqVO.getClientIds());
        pageReqVO.setClientIds(authClientIds);
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        
        List<ProfitReportRespVO> exportData;
        String fileName;
        
        // 根据维度参数决定导出逻辑
        if ("sku".equals(pageReqVO.getDimension())) {
            // SKU维度导出
            IPage<ProfitReportRespVO> iPageResult = new Page<>(1, Integer.MAX_VALUE);
            profitReportService.getSkuProfitReportPage(iPageResult, pageReqVO);
            List<ProfitReportRespVO> records = iPageResult.getRecords();
            ozonProfitService.handleSkuProfitReport(records);
            exportData = records;
            fileName = "财务账单报告_SKU维度.xls";
        } else {
            // 门店维度导出（默认）
            IPage<ProfitReportRespVO> iPageResult = new Page<>(1, Integer.MAX_VALUE);
            profitReportService.getClientProfitReportPage(iPageResult, pageReqVO);
            List<ProfitReportRespVO> records = iPageResult.getRecords();
            ozonProfitService.handleClientProfitReport(pageReqVO, records);
            exportData = records;
            fileName = "财务账单报告_门店维度.xls";
        }
        
        // 导出 Excel
        ExcelUtils.write(response, fileName, "数据", ProfitReportRespVO.class, exportData);
    }

    @PostMapping("/refresh")
    @Operation(summary = "强制刷新财务账单报告")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:refresh')")
    public CommonResult<ProfitReportTaskLogRespVO> refreshProfit(@Valid @RequestBody ProfitReportRefreshReqVO reqVO){

        if (reqVO.getFinanceDate() == null) {
            return success(null);
        }

        String[] authClientIds = getClientIds(reqVO.getClientIds());
        reqVO.setClientIds(authClientIds);

        // 解析日期字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(reqVO.getFinanceDate()[0], formatter);
        LocalDate endDate = LocalDate.parse(reqVO.getFinanceDate()[1], formatter);
        
        // 创建任务日志并返回任务ID
        String taskId = profitReportTaskLogService.createTaskLog(
                authClientIds, 
                reqVO.getDimension(), 
                reqVO.getTimeType(), 
                startDate, 
                endDate);
        
        // 异步执行计算
        profitComputeManagerService.handleComputeProfit(
                taskId,
                Arrays.asList(authClientIds), 
                reqVO.getFinanceDate()[0], 
                reqVO.getFinanceDate()[1],
                reqVO.getDimension(),
                reqVO.getTimeType());

        // 返回任务日志信息
        ProfitReportTaskLogDO taskLog = profitReportTaskLogService.getTaskLog(taskId);
        return success(BeanUtils.toBean(taskLog, ProfitReportTaskLogRespVO.class));
    }


    private String[] getClientIds(String[] clientIds) {
        if (null == clientIds || clientIds.length == 0) {
            List<OzonShopMappingDO> authShopList = authShopMappingService.getAuthShopList();
            return convertList(authShopList, OzonShopMappingDO::getClientId).toArray(new String[0]);
        }
        return clientIds;
    }

    // 通用的安全除法方法，防止除以0的情况和空指针异常
    private BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
    }
}