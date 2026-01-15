package cn.iocoder.yudao.module.chrome.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * Chrome插件大盘统计数据响应VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - Chrome插件大盘统计数据 Response VO")
@Data
public class DashboardStatisticsRespVO {

    @Schema(description = "统计日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01")
    private LocalDate statisticsDate;

    // ========== 用户维度 ==========
    @Schema(description = "用户统计")
    private UserStatistics userStatistics;

    // ========== 积分维度 ==========
    @Schema(description = "积分统计")
    private CreditsStatistics creditsStatistics;

    // ========== 订单维度 ==========
    @Schema(description = "订单统计")
    private OrderStatistics orderStatistics;

    // ========== 功能维度 ==========
    @Schema(description = "功能统计")
    private FeatureStatistics featureStatistics;

    // ========== 用户统计 ==========
    @Data
    @Schema(description = "用户统计")
    public static class UserStatistics {
        @Schema(description = "总用户数", example = "1000")
        private Integer totalUserCount;

        @Schema(description = "新增用户数", example = "50")
        private Integer newUserCount;

        @Schema(description = "活跃用户数", example = "100")
        private Integer activeUserCount;
    }

    // ========== 积分统计 ==========
    @Data
    @Schema(description = "积分统计")
    public static class CreditsStatistics {
        @Schema(description = "充值积分总量", example = "1000")
        private Integer totalRechargeCredits;

        @Schema(description = "消费积分总量", example = "500")
        private Integer totalConsumeCredits;

        @Schema(description = "无结果查询次数", example = "10")
        private Integer noDataQueryCount;
    }

    // ========== 订单统计 ==========
    @Data
    @Schema(description = "订单统计")
    public static class OrderStatistics {
        @Schema(description = "订单总数", example = "100")
        private Integer totalOrderCount;

        @Schema(description = "已支付订单数", example = "80")
        private Integer paidOrderCount;

        @Schema(description = "订单总金额", example = "10000.00")
        private java.math.BigDecimal totalAmount;

        @Schema(description = "已支付金额", example = "8000.00")
        private java.math.BigDecimal paidAmount;

        @Schema(description = "支付成功率", example = "80.00")
        private java.math.BigDecimal paymentSuccessRate;
    }

    // ========== 功能统计 ==========
    @Data
    @Schema(description = "功能统计")
    public static class FeatureStatistics {
        @Schema(description = "总使用次数", example = "500")
        private Integer totalUsageCount;

        @Schema(description = "各功能消耗明细")
        private FeatureConsumeDetail featureConsumeDetail;
    }

    // ========== 功能消耗明细 ==========
    @Data
    @Schema(description = "各功能消耗明细")
    public static class FeatureConsumeDetail {

        @Schema(description = "商品采集消耗积分", example = "50")
        private Integer productCollectCredits;

        @Schema(description = "排名采集消耗积分", example = "30")
        private Integer rankingCollectCredits;

        @Schema(description = "评论采集消耗积分", example = "20")
        private Integer commentCollectCredits;

        @Schema(description = "销量采集消耗积分", example = "40")
        private Integer salesCollectCredits;

        @Schema(description = "趋势采集消耗积分", example = "60")
        private Integer trendCollectCredits;

        @Schema(description = "类目分析消耗积分", example = "100")
        private Integer categoryAnalysisCredits;

        @Schema(description = "飞书导出消耗积分", example = "10")
        private Integer feishuExportCredits;

        @Schema(description = "Excel导出消耗积分", example = "15")
        private Integer excelExportCredits;
    }
}
