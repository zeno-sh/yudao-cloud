package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.TradeTrendReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.mysql.order.OzonOrderMapper;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Zeno
 * @createTime: 2024/07/03 17:16
 */
@Service
public class OzonTradeStatisticsService {

    @Resource
    private OzonOrderMapper orderMapper;

    public TradeTrendSummaryRespVO getTradeTrendSummary(TradeTrendReqVO reqVO) {
        String[] clientIds = reqVO.getClientIds();
        String beginDateTime = DmDateUtils.formatStartOfDay(reqVO.getTimes()[0], DatePattern.NORM_DATETIME_PATTERN);
        String endDateTime = DmDateUtils.formatEndOfDay(reqVO.getTimes()[1], DatePattern.NORM_DATETIME_PATTERN);

        return orderMapper.selectVoByTimeBetween(clientIds,
                LocalDateTimeUtil.of(DateUtil.parseDateTime(beginDateTime).toInstant()),
                LocalDateTimeUtil.of(DateUtil.parseDateTime(endDateTime).toInstant()));
    }

    public List<TradeTrendSummaryRespVO> getTradeTendSummaryList(TradeTrendReqVO reqVO) {
        String[] clientIds = reqVO.getClientIds();

        String beginDateTime = DmDateUtils.formatStartOfDay(reqVO.getTimes()[0], DatePattern.NORM_DATETIME_PATTERN);
        String endDateTime = DmDateUtils.formatEndOfDay(reqVO.getTimes()[1], DatePattern.NORM_DATETIME_PATTERN);

        List<OzonOrderDO> orderList = orderMapper.selectList(new LambdaQueryWrapperX<OzonOrderDO>()
                .inIfPresent(OzonOrderDO::getClientId, clientIds)
                .betweenIfPresent(OzonOrderDO::getInProcessAt,
                        LocalDateTimeUtil.of(DateUtil.parseDateTime(beginDateTime).toInstant()),
                        LocalDateTimeUtil.of(DateUtil.parseDateTime(endDateTime).toInstant()))
        );

        // 按照yyyy-MM-dd格式分组并统计
        Map<String, TradeTrendSummaryRespVO> summaryMap = orderList.stream().collect(Collectors.groupingBy(
                order -> DmDateUtils.convertUtcToMoscowLocalDate(order.getInProcessAt()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                Collectors.collectingAndThen(Collectors.toList(), orders -> {
                    TradeTrendSummaryRespVO summary = new TradeTrendSummaryRespVO();
                    LocalDate moscowLocalDate = DmDateUtils.convertUtcToMoscowLocalDate(orders.get(0).getInProcessAt());
                    summary.setDate(moscowLocalDate);
                    summary.setTotalOrders(orders.size());
                    summary.setTotalSales(orders.stream()
                            .map(OzonOrderDO::getAccrualsForSale)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return summary;
                })
        ));

       return  summaryMap.values().stream()
                .sorted(Comparator.comparing(TradeTrendSummaryRespVO::getDate))
                .collect(Collectors.toList());
    }
}
