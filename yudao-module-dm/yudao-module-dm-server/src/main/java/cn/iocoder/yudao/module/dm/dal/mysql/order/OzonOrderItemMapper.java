package cn.iocoder.yudao.module.dm.dal.mysql.order;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.ProductVolumeReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import cn.iocoder.yudao.module.dm.infrastructure.service.dto.ProductVolumeDTO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单商品详情 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface OzonOrderItemMapper extends BaseMapperX<OzonOrderItemDO> {

    default List<OzonOrderItemDO> selectListByOrderId(String clientId, String orderId) {
        return selectList(OzonOrderItemDO::getClientId, clientId, OzonOrderItemDO::getOrderId, orderId);
    }

    default int deleteByOrderId(String orderId) {
        return delete(OzonOrderItemDO::getOrderId, orderId);
    }


    default PageResult<OzonOrderItemDO> selectItemPage(ProductVolumeReqVO reqVO) {

        MPJLambdaWrapper<OzonOrderItemDO> wrapper = new MPJLambdaWrapper<>();

        wrapper.select("t.platform_sku_id, t.offer_id, t.client_id")
                .select("COALESCE(SUM(t.quantity), 0) AS quantity")
                .select("COALESCE(SUM(t.price), 0) AS totalPrice")
                .groupBy("t.platform_sku_id", "t.client_id",
                        "t.offer_id")
                .having("quantity > 0") // 过滤 quantity > 0 的结果
                .orderByDesc("quantity"); // 按照 quantity 倒排序


        // 时区转换 -> 莫斯科时区
        LocalDateTime[] inProcessAtMoscowLocalDateTimes = new LocalDateTime[2];
        if (null == reqVO.getDate() || reqVO.getDate().length == 0) {
            inProcessAtMoscowLocalDateTimes = null;
        } else {
            String beginDateTime = DmDateUtils.formatStartOfDay(reqVO.getDate()[0], DatePattern.NORM_DATETIME_PATTERN);
            String endDateTime = DmDateUtils.formatEndOfDay(reqVO.getDate()[1], DatePattern.NORM_DATETIME_PATTERN);
            inProcessAtMoscowLocalDateTimes[0] = LocalDateTimeUtil.of(DateUtil.parseDateTime(beginDateTime).toInstant());
            inProcessAtMoscowLocalDateTimes[1] = LocalDateTimeUtil.of(DateUtil.parseDateTime(endDateTime).toInstant());
        }
        if (reqVO.getDate() != null && reqVO.getDate().length == 2) {
            wrapper.between(OzonOrderItemDO::getInProcessAt, inProcessAtMoscowLocalDateTimes[0], inProcessAtMoscowLocalDateTimes[1]);
        }

        if (StringUtils.isNotBlank(reqVO.getOfferId())) {
            wrapper.like(OzonOrderItemDO::getOfferId, reqVO.getOfferId());
        }

        if (reqVO.getClientIds() != null && reqVO.getClientIds().length > 0) {
            wrapper.in(OzonOrderItemDO::getClientId, reqVO.getClientIds());
        }


        return selectJoinPage(reqVO, OzonOrderItemDO.class, wrapper);
    }

    default PageResult<OzonOrderItemDO> selectSimplePage(ProductVolumeReqVO reqVO) {

        // 时区转换 -> 莫斯科时区
        LocalDateTime[] inProcessAtMoscowLocalDateTimes = new LocalDateTime[2];
        if (null == reqVO.getDate() || reqVO.getDate().length == 0) {
            inProcessAtMoscowLocalDateTimes = null;
        } else {
            String beginDateTime = DmDateUtils.formatStartOfDay(reqVO.getDate()[0], DatePattern.NORM_DATETIME_PATTERN);
            String endDateTime = DmDateUtils.formatEndOfDay(reqVO.getDate()[1], DatePattern.NORM_DATETIME_PATTERN);
            inProcessAtMoscowLocalDateTimes[0] = LocalDateTimeUtil.of(DateUtil.parseDateTime(beginDateTime).toInstant());
            inProcessAtMoscowLocalDateTimes[1] = LocalDateTimeUtil.of(DateUtil.parseDateTime(endDateTime).toInstant());
        }

        return selectPage(reqVO, new LambdaQueryWrapperX<OzonOrderItemDO>()
                .inIfPresent(OzonOrderItemDO::getClientId, reqVO.getClientIds())
                .eqIfPresent(OzonOrderItemDO::getOfferId, reqVO.getOfferId())
                .betweenIfPresent(OzonOrderItemDO::getInProcessAt, inProcessAtMoscowLocalDateTimes)
                .orderByDesc(OzonOrderItemDO::getInProcessAt));
    }

    /**
     * 查询指定月份的销售统计数据
     *
     * @param clientIds 店铺编号列表
     * @param month 月份，格式：yyyy-MM
     * @param sku SKU
     * @return 销售统计数据
     */
    List<Map<String, Object>> selectMonthSalesStats(@Param("clientIds") String[] clientIds,
                                                   @Param("month") String month,
                                                   @Param("sku") String sku);

    /**
     * 查询历史销售统计数据（指定月份之前的所有销售数据）
     *
     * @param clientIds 店铺编号列表
     * @param month 月份，格式：yyyy-MM
     * @param sku SKU
     * @return 销售统计数据
     */
    List<Map<String, Object>> selectHistorySalesStats(@Param("clientIds") String[] clientIds,
                                                     @Param("month") String month,
                                                     @Param("sku") String sku);

    /**
     * 按门店维度查询销量统计数据
     * 
     * @param page MyBatis-Plus分页对象
     * @param reqVO 查询条件
     * @param beginDateTime 开始时间
     * @param endDateTime 结束时间
     * @return 按门店统计的销量数据
     */
    IPage<OzonOrderItemDO> selectShopOrderItemPage(IPage<OzonOrderItemDO> page, 
                                                  @Param("reqVO") ProductVolumeReqVO reqVO,
                                                  @Param("beginDateTime") LocalDateTime beginDateTime,
                                                  @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 按SKU维度查询销量统计数据
     * 
     * @param page MyBatis-Plus分页对象
     * @param reqVO 查询条件
     * @param beginDateTime 开始时间
     * @param endDateTime 结束时间
     * @return 按SKU统计的销量数据
     */
    IPage<OzonOrderItemDO> selectSkuOrderItemPage(IPage<OzonOrderItemDO> page, 
                                                 @Param("reqVO") ProductVolumeReqVO reqVO,
                                                 @Param("beginDateTime") LocalDateTime beginDateTime,
                                                 @Param("endDateTime") LocalDateTime endDateTime);


    List<ProductVolumeDTO> selectSkuOrderItem(@Param("reqVO") ProductVolumeReqVO reqVO,
                                              @Param("beginDateTime") LocalDateTime beginDateTime,
                                              @Param("endDateTime") LocalDateTime endDateTime);
}