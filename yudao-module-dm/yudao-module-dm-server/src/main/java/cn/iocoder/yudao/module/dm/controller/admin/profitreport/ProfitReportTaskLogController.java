package cn.iocoder.yudao.module.dm.controller.admin.profitreport;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportTaskLogPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportTaskLogRespVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportTaskLogDO;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 财务账单报告计算任务日志
 *
 * @author zeno
 */
@Tag(name = "管理后台 - 财务账单报告计算任务日志")
@RestController
@RequestMapping("/dm/profit-report-task-log")
@Validated
public class ProfitReportTaskLogController {

    @Resource
    private ProfitReportTaskLogService profitReportTaskLogService;

    @GetMapping("/page")
    @Operation(summary = "获得任务日志分页")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:query')")
    public CommonResult<PageResult<ProfitReportTaskLogRespVO>> getTaskLogPage(@Valid ProfitReportTaskLogPageReqVO pageReqVO) {
        PageResult<ProfitReportTaskLogDO> pageResult = profitReportTaskLogService.getTaskLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProfitReportTaskLogRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得任务日志详情")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('dm:profit-report:query')")
    public CommonResult<ProfitReportTaskLogRespVO> getTaskLog(@RequestParam("taskId") String taskId) {
        ProfitReportTaskLogDO taskLog = profitReportTaskLogService.getTaskLog(taskId);
        return success(BeanUtils.toBean(taskLog, ProfitReportTaskLogRespVO.class));
    }

    @GetMapping("/latest")
    @Operation(summary = "获得最近的任务日志")
    @Parameter(name = "limit", description = "限制条数", required = true)
    @PreAuthorize("@ss.hasPermission('dm:profit-report:query')")
    public CommonResult<List<ProfitReportTaskLogRespVO>> getLatestTaskLogs(@RequestParam("limit") int limit) {
        List<ProfitReportTaskLogDO> list = profitReportTaskLogService.getLatestTaskLogs(limit);
        return success(BeanUtils.toBean(list, ProfitReportTaskLogRespVO.class));
    }
} 