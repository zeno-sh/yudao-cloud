package cn.iocoder.yudao.module.dm.controller.admin.profitreport;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.*;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportTaskLogRespVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportTaskLogDO;
import cn.iocoder.yudao.module.dm.infrastructure.AuthShopMappingService;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.ProfitCalculationOrchestratorService;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 财务账单报告重构版接口
 *
 * @author Jax
 */
@Tag(name = "管理后台 - 财务账单报告V2")
@RestController
@RequestMapping("/dm/profit-report/v2")
@Validated
@Slf4j
public class ProfitReportV2Controller {
    
    @Resource
    private ProfitCalculationOrchestratorService profitCalculationOrchestratorService;
    @Resource
    private AuthShopMappingService authShopMappingService;
    @Resource
    private ProfitReportTaskLogService profitReportTaskLogService;
    
    /**
     * 异步计算财务账单报告（参考V1版本的交互方式）
     */
    @PostMapping("/calculate")
    @Operation(summary = "异步计算财务账单报告")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:calculate')")
    public CommonResult<ProfitReportTaskLogRespVO> calculateProfitReport(@Valid @RequestBody ProfitCalculationRequestVO request) {
        
        // 参数验证 - 参考V1版本的优雅验证方式
        if (request.getFinanceDate() == null || request.getFinanceDate().length != 2) {
            log.warn("V2版本利润计算请求参数错误: 财务日期范围不完整 - {}", 
                    request.getFinanceDate() != null ? Arrays.toString(request.getFinanceDate()) : "null");
            return success(null);
        }

        // 获取权限内的门店ID - 参考V1版本的权限处理方式
        String[] authClientIds = getClientIds(request.getClientIds());
        request.setClientIds(authClientIds);

        // 解析日期字符串 - 参考V1版本的日期处理方式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(request.getFinanceDate()[0], formatter);
            endDate = LocalDate.parse(request.getFinanceDate()[1], formatter);
        } catch (Exception e) {
            log.error("V2版本利润计算日期解析失败: {}", Arrays.toString(request.getFinanceDate()), e);
            return success(null);
        }
        
        log.info("收到V2版本利润计算请求: 门店={}, 日期范围={}-{}, 维度={}, 时间类型={}", 
                String.join(",", authClientIds), startDate, endDate, 
                request.getDimension(), request.getTimeType());
        
        // 创建任务日志 - 参考V1版本的任务管理方式
        String taskId = profitReportTaskLogService.createTaskLog(
                authClientIds, 
                request.getDimension(), 
                request.getTimeType(), 
                startDate, 
                endDate);
        
        // 立即获取任务日志信息，准备返回给客户端 - 确保真正的异步
        ProfitReportTaskLogDO taskLog = profitReportTaskLogService.getTaskLog(taskId);
        ProfitReportTaskLogRespVO result = BeanUtils.toBean(taskLog, ProfitReportTaskLogRespVO.class);
        
        // 预估处理时间
        Integer estimatedTime = estimateProcessingTime(request);
        
        log.info("V2版本利润计算任务创建成功: taskId={}, 预计处理时间={}分钟", 
                taskId, estimatedTime);
        
        // 异步执行计算 - 在返回响应之前触发异步任务
        // 使用CompletableFuture.runAsync确保真正的异步执行
        CompletableFuture.runAsync(() -> {
            try {
                profitCalculationOrchestratorService.executeCalculationAsyncV1Compatible(
                        taskId,
                        Arrays.asList(authClientIds), 
                        request.getFinanceDate()[0], 
                        request.getFinanceDate()[1],
                        request.getDimension(),
                        request.getTimeType());
            } catch (Exception e) {
                log.error("V2版本异步计算任务执行失败: taskId={}", taskId, e);
                // 记录错误到任务日志
                profitReportTaskLogService.appendErrorInfo(taskId, "异步计算任务执行失败: " + e.getMessage());
            }
        });
        
        // 立即返回任务信息给客户端，实现真正的异步
        return success(result);
    }
    
    /**
     * 获取计算结果
     */
    @GetMapping("/result")
    @Operation(summary = "获取计算结果")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:query')")
    public CommonResult<PageResult<ProfitReportResultVO>> getProfitReportResult(@Valid ProfitReportQueryReqVO queryReqVO) {
        log.info("查询V2版本利润计算结果: taskId={}, 门店={}, 维度={}", 
                queryReqVO.getTaskId(), queryReqVO.getClientId(), queryReqVO.getDimension());
        
        PageResult<ProfitReportResultVO> pageResult = profitCalculationOrchestratorService.getCalculationResult(queryReqVO);
        
        log.info("V2版本利润计算结果查询完成: 总记录数={}, 当前页记录数={}", 
                pageResult.getTotal(), pageResult.getList().size());
        
        return success(pageResult);
    }
    
    /**
     * 导出计算结果
     */
    @GetMapping("/export")
    @Operation(summary = "导出计算结果")
    @PreAuthorize("@ss.hasPermission('dm:profit-report:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProfitReport(@Valid ProfitReportQueryReqVO queryReqVO, HttpServletResponse response) {
        log.info("导出V2版本利润计算结果: taskId={}, 门店={}, 维度={}", 
                queryReqVO.getTaskId(), queryReqVO.getClientId(), queryReqVO.getDimension());
        
        profitCalculationOrchestratorService.exportCalculationResult(queryReqVO, response);
        
        log.info("V2版本利润计算结果导出完成");
    }
    
    /**
     * 取消计算任务
     */
    @PostMapping("/task/{taskId}/cancel")
    @Operation(summary = "取消计算任务")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('dm:profit-report:cancel')")
    public CommonResult<Boolean> cancelTask(@PathVariable String taskId) {
        log.info("取消V2版本利润计算任务: taskId={}", taskId);
        
        Boolean result = profitCalculationOrchestratorService.cancelCalculation(taskId);
        
        log.info("V2版本利润计算任务取消结果: taskId={}, 成功={}", taskId, result);
        
        return success(result);
    }

    /**
     * 获取权限内的门店ID - 参考V1版本的实现
     */
    private String[] getClientIds(String[] clientIds) {
        if (null == clientIds || clientIds.length == 0) {
            List<OzonShopMappingDO> authShopList = authShopMappingService.getAuthShopList();
            return convertList(authShopList, OzonShopMappingDO::getClientId).toArray(new String[0]);
        }
        return clientIds;
    }

    /**
     * 预估处理时间 - 参考V1版本的时间估算逻辑
     */
    private Integer estimateProcessingTime(ProfitCalculationRequestVO request) {
        int baseTime = 2; // 基础时间2分钟
        
        try {
            if (request.getFinanceDate() != null && request.getFinanceDate().length >= 2) {
                LocalDate startDate = LocalDate.parse(request.getFinanceDate()[0]);
                LocalDate endDate = LocalDate.parse(request.getFinanceDate()[1]);
                long days = startDate.until(endDate).getDays() + 1;
                
                // 每天增加0.5分钟
                baseTime += (int) (days * 0.5);
                
                // 根据维度调整时间
                if ("sku".equals(request.getDimension())) {
                    baseTime *= 1.5; // SKU维度需要更多时间
                }
                
                // 根据门店数量调整时间
                if (request.getClientIds() != null && request.getClientIds().length > 1) {
                    baseTime += request.getClientIds().length * 0.5;
                }
            }
        } catch (Exception e) {
            log.warn("解析日期范围失败，使用默认处理时间: {}", e.getMessage());
        }
        
        return Math.max(baseTime, 2); // 最少2分钟
    }
} 