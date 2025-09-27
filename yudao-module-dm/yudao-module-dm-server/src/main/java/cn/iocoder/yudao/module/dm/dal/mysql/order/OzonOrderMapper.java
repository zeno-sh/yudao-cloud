package cn.iocoder.yudao.module.dm.dal.mysql.order;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.order.vo.OzonOrderPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderSalesStatsDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Ozon订单 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface OzonOrderMapper extends BaseMapperX<OzonOrderDO> {

    default PageResult<OzonOrderDO> selectPage(OzonOrderPageReqVO reqVO) {

        // 时区转换 -> 莫斯科时区
        LocalDateTime[] inProcessAtMoscowLocalDateTimes = new LocalDateTime[2];
        if (null == reqVO.getInProcessAt() || reqVO.getInProcessAt().length == 0) {
            inProcessAtMoscowLocalDateTimes = null;
        } else {
            String beginDateTime = DmDateUtils.formatStartOfDay(reqVO.getInProcessAt()[0], DatePattern.NORM_DATETIME_PATTERN);
            String endDateTime = DmDateUtils.formatEndOfDay(reqVO.getInProcessAt()[1], DatePattern.NORM_DATETIME_PATTERN);
            inProcessAtMoscowLocalDateTimes[0] = LocalDateTimeUtil.of(DateUtil.parseDateTime(beginDateTime).toInstant());
            inProcessAtMoscowLocalDateTimes[1] = LocalDateTimeUtil.of(DateUtil.parseDateTime(endDateTime).toInstant());
        }

        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;
        if (StringUtils.isNotBlank(reqVO.getTodayShipmentDate())) {
            // 将日期字符串转换为 LocalDate 对象
            LocalDate shipmentDate = LocalDate.parse(reqVO.getTodayShipmentDate());

            // 获取当天的开始时间和结束时间
            startOfDay = shipmentDate.atStartOfDay();
            endOfDay = shipmentDate.atTime(LocalTime.MAX);
        }

        return selectPage(reqVO, new LambdaQueryWrapperX<OzonOrderDO>()
                .inIfPresent(OzonOrderDO::getClientId, reqVO.getClientIds())
                .eqIfPresent(OzonOrderDO::getOrderType, reqVO.getOrderType())
                .eqIfPresent(OzonOrderDO::getOrderId, reqVO.getOrderId())
                .likeIfPresent(OzonOrderDO::getPostingNumber, reqVO.getPostingNumber())
                .eqIfPresent(OzonOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(OzonOrderDO::getInProcessAt, inProcessAtMoscowLocalDateTimes)
                .betweenIfPresent(OzonOrderDO::getShipmentDate, startOfDay, endOfDay)
                .betweenIfPresent(OzonOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OzonOrderDO::getInProcessAt));
    }

    TradeTrendSummaryRespVO selectVoByTimeBetween(@Param("clientIds") String[] clientIds,
                                                  @Param("beginTime") LocalDateTime beginTime,
                                                  @Param("endTime") LocalDateTime endTime);


    IPage<OzonOrderDO> selectPage2(IPage<OzonOrderDO> page, @Param("reqVO") OzonOrderPageReqVO reqVO);

    /**
     * 查询指定月份的销售统计数据
     *
     * @param clientIds 店铺编号列表
     * @param month 月份，格式为yyyy-MM
     * @param offerId 商品Offer ID
     * @return 销售统计数据列表
     */
    List<OzonOrderSalesStatsDO> selectMonthSalesStats(@Param("clientIds") String[] clientIds, 
            @Param("month") String month, 
            @Param("offerId") String offerId);

    /**
     * 查询历史销售统计数据（指定月份之前的所有销售数据）
     *
     * @param clientIds 店铺编号列表
     * @param month 月份，格式为yyyy-MM
     * @param offerId 商品Offer ID
     * @return 销售统计数据列表
     */
    List<OzonOrderSalesStatsDO> selectHistorySalesStats(@Param("clientIds") String[] clientIds, 
            @Param("month") String month, 
            @Param("offerId") String offerId);
}