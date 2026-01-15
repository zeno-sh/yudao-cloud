package cn.iocoder.yudao.module.chrome.service.dashboard;

import cn.iocoder.yudao.module.chrome.controller.admin.dashboard.vo.DashboardStatisticsRespVO;
import cn.iocoder.yudao.module.chrome.controller.admin.dashboard.vo.UserCreditsConsumeRespVO;
import cn.iocoder.yudao.module.chrome.dal.mysql.order.SubscriptionOrderMapper;
import cn.iocoder.yudao.module.chrome.dal.mysql.transaction.CreditsTransactionMapper;
import cn.iocoder.yudao.module.chrome.dal.mysql.usage.UsageRecordMapper;
import cn.iocoder.yudao.module.chrome.dal.mysql.user.ChromeUserMapper;
import cn.iocoder.yudao.module.chrome.enums.FeatureTypeEnum;
import cn.iocoder.yudao.module.chrome.enums.TransactionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Chrome插件大盘统计 Service 实现类
 *
 * @author Jax
 */
@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private ChromeUserMapper chromeUserMapper;

    @Resource
    private UsageRecordMapper usageRecordMapper;

    @Resource
    private CreditsTransactionMapper creditsTransactionMapper;

    @Resource
    private SubscriptionOrderMapper subscriptionOrderMapper;

    @Override
    public DashboardStatisticsRespVO getStatistics(LocalDate date) {
        String dateStr = date.toString();
        
        DashboardStatisticsRespVO respVO = new DashboardStatisticsRespVO();
        respVO.setStatisticsDate(date);

        // ========== 1. 用户统计 ==========
        DashboardStatisticsRespVO.UserStatistics userStats = new DashboardStatisticsRespVO.UserStatistics();
        userStats.setTotalUserCount(chromeUserMapper.countTotalUsers());
        userStats.setNewUserCount(chromeUserMapper.countNewUsersByDate(dateStr));
        userStats.setActiveUserCount(chromeUserMapper.countActiveUsersByDate(dateStr));
        respVO.setUserStatistics(userStats);

        // ========== 2. 积分统计 ==========
        DashboardStatisticsRespVO.CreditsStatistics creditsStats = new DashboardStatisticsRespVO.CreditsStatistics();
        creditsStats.setTotalRechargeCredits(creditsTransactionMapper.sumCreditsByDateAndType(
            dateStr, TransactionTypeEnum.RECHARGE.getCode()));
        creditsStats.setTotalConsumeCredits(usageRecordMapper.sumCreditsConsumedByDate(dateStr));
        creditsStats.setNoDataQueryCount(creditsTransactionMapper.countByDateAndType(
            dateStr, TransactionTypeEnum.API_CALL_NO_DATA.getCode()));
        respVO.setCreditsStatistics(creditsStats);

        // ========== 3. 订单统计 ==========
        DashboardStatisticsRespVO.OrderStatistics orderStats = new DashboardStatisticsRespVO.OrderStatistics();
        Map<String, Object> orderData = subscriptionOrderMapper.statisticsOrderByDate(dateStr);
        
        orderStats.setTotalOrderCount(((Number) orderData.getOrDefault("orderCount", 0)).intValue());
        orderStats.setPaidOrderCount(((Number) orderData.getOrDefault("paidOrderCount", 0)).intValue());
        orderStats.setTotalAmount(new BigDecimal(orderData.getOrDefault("totalAmount", "0").toString()));
        orderStats.setPaidAmount(new BigDecimal(orderData.getOrDefault("paidAmount", "0").toString()));
        
        // 计算支付成功率
        if (orderStats.getTotalOrderCount() > 0) {
            BigDecimal rate = BigDecimal.valueOf(orderStats.getPaidOrderCount())
                .divide(BigDecimal.valueOf(orderStats.getTotalOrderCount()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            orderStats.setPaymentSuccessRate(rate);
        } else {
            orderStats.setPaymentSuccessRate(BigDecimal.ZERO);
        }
        respVO.setOrderStatistics(orderStats);

        // ========== 4. 功能统计 ==========
        DashboardStatisticsRespVO.FeatureStatistics featureStats = new DashboardStatisticsRespVO.FeatureStatistics();
        
        // 统计各功能消耗明细
        DashboardStatisticsRespVO.FeatureConsumeDetail featureDetail = new DashboardStatisticsRespVO.FeatureConsumeDetail();
        featureDetail.setProductCollectCredits(usageRecordMapper.sumCreditsConsumedByDateAndFeature(
            dateStr, FeatureTypeEnum.PRODUCT_COLLECT.getType()));
        featureDetail.setRankingCollectCredits(usageRecordMapper.sumCreditsConsumedByDateAndFeature(
            dateStr, FeatureTypeEnum.RANKING_COLLECT.getType()));
        featureDetail.setCommentCollectCredits(usageRecordMapper.sumCreditsConsumedByDateAndFeature(
            dateStr, FeatureTypeEnum.COMMENT_COLLECT.getType()));
        featureDetail.setSalesCollectCredits(usageRecordMapper.sumCreditsConsumedByDateAndFeature(
            dateStr, FeatureTypeEnum.SALES_COLLECT.getType()));
        featureDetail.setTrendCollectCredits(usageRecordMapper.sumCreditsConsumedByDateAndFeature(
            dateStr, FeatureTypeEnum.TREND_COLLECT.getType()));
        featureDetail.setCategoryAnalysisCredits(usageRecordMapper.sumCreditsConsumedByDateAndFeature(
            dateStr, FeatureTypeEnum.CATEGORY_ANALYSIS.getType()));
        featureDetail.setFeishuExportCredits(usageRecordMapper.sumCreditsConsumedByDateAndFeature(
            dateStr, FeatureTypeEnum.FEISHU_EXPORT.getType()));
        featureDetail.setExcelExportCredits(usageRecordMapper.sumCreditsConsumedByDateAndFeature(
            dateStr, FeatureTypeEnum.EXCEL_EXPORT.getType()));
        
        featureStats.setFeatureConsumeDetail(featureDetail);
        
        // 计算总使用次数
        int totalUsageCount = featureDetail.getProductCollectCredits() + 
                             featureDetail.getRankingCollectCredits() +
                             featureDetail.getCommentCollectCredits() +
                             featureDetail.getSalesCollectCredits() +
                             featureDetail.getTrendCollectCredits() +
                             featureDetail.getCategoryAnalysisCredits() +
                             featureDetail.getFeishuExportCredits() +
                             featureDetail.getExcelExportCredits();
        featureStats.setTotalUsageCount(totalUsageCount);
        
        respVO.setFeatureStatistics(featureStats);

        log.info("统计日期: {}, 用户: 总{}/新增{}/活跃{}, 积分: 充值{}/消耗{}, 订单: {}/金额{}", 
            dateStr, userStats.getTotalUserCount(), userStats.getNewUserCount(), userStats.getActiveUserCount(),
            creditsStats.getTotalRechargeCredits(), creditsStats.getTotalConsumeCredits(),
            orderStats.getTotalOrderCount(), orderStats.getTotalAmount());

        return respVO;
    }

    @Override
    public List<UserCreditsConsumeRespVO> getUserCreditsConsume(LocalDate date) {
        String dateStr = date.toString();
        
        List<Map<String, Object>> rawData = usageRecordMapper.getUserCreditsConsumeByDate(dateStr);
        List<UserCreditsConsumeRespVO> result = new ArrayList<>();
        
        for (Map<String, Object> data : rawData) {
            UserCreditsConsumeRespVO vo = new UserCreditsConsumeRespVO();
            vo.setUserId(((Number) data.get("userId")).longValue());
            vo.setEmail((String) data.get("email"));
            vo.setNickname((String) data.get("nickname"));
            vo.setCreditsConsumed(((Number) data.get("creditsConsumed")).intValue());
            vo.setUsageCount(((Number) data.get("usageCount")).intValue());
            result.add(vo);
        }
        
        log.info("统计日期: {}, 共{}个用户有积分消耗", dateStr, result.size());
        
        return result;
    }
}
