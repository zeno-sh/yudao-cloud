package cn.iocoder.yudao.module.chrome.controller.admin.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.chrome.controller.admin.dashboard.vo.DashboardStatisticsRespVO;
import cn.iocoder.yudao.module.chrome.controller.admin.dashboard.vo.UserCreditsConsumeRespVO;
import cn.iocoder.yudao.module.chrome.infra.dto.CoupangTrendsResponseDTO;
import cn.iocoder.yudao.module.chrome.infra.service.TrendsQueryService;
import cn.iocoder.yudao.module.chrome.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Chrome插件大盘控制器
 *
 * @author Jax
 */
@Tag(name = "管理后台 - Chrome插件大盘")
@RestController
@RequestMapping("/chrome/dashboard")
@Validated
@Slf4j
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @Resource
    private TrendsQueryService trendsQueryService;

    @GetMapping("/test")
    @Operation(summary = "测试服务是否正常（查询iPad关键词）")
    @PreAuthorize("@ss.hasPermission('chrome:dashboard:query')")
    public CommonResult<CoupangTrendsResponseDTO> test() {
        log.info("执行服务健康检查 - 查询iPad关键词趋势");
        
        CoupangTrendsResponseDTO result = trendsQueryService.queryKeywordTrends(
            "iPad", 0, 10, null);
        
        if (result != null && result.getSearchItems() != null && !result.getSearchItems().isEmpty()) {
            log.info("服务健康检查成功，查询到{}个商品", result.getSearchItems().size());
            return success(result);
        } else {
            log.warn("服务健康检查异常，未查询到商品数据");
            return success(result);
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据")
    @PreAuthorize("@ss.hasPermission('chrome:dashboard:query')")
    @Parameter(name = "date", description = "统计日期，默认为当天", example = "2024-01-01")
    public CommonResult<DashboardStatisticsRespVO> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        // 默认统计当天数据
        if (date == null) {
            date = LocalDate.now();
        }
        
        log.info("查询Chrome插件大盘统计数据，日期: {}", date);
        DashboardStatisticsRespVO statistics = dashboardService.getStatistics(date);
        
        return success(statistics);
    }

    @GetMapping("/user-credits-consume")
    @Operation(summary = "获取用户积分消耗明细")
    @PreAuthorize("@ss.hasPermission('chrome:dashboard:query')")
    @Parameter(name = "date", description = "统计日期，默认为当天", example = "2024-01-01")
    public CommonResult<List<UserCreditsConsumeRespVO>> getUserCreditsConsume(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        // 默认统计当天数据
        if (date == null) {
            date = LocalDate.now();
        }
        
        log.info("查询用户积分消耗明细，日期: {}", date);
        List<UserCreditsConsumeRespVO> result = dashboardService.getUserCreditsConsume(date);
        
        return success(result);
    }
}
