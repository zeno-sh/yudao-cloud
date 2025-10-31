package cn.iocoder.yudao.module.dm.service.order;

import java.time.LocalDateTime;
import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.ProductVolumeReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.infrastructure.service.dto.ProductVolumeDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

/**
 * Ozon订单 Service 接口
 *
 * @author Zeno
 */
public interface OzonOrderService {

    /**
     * 创建Ozon订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOzonOrder(@Valid OzonOrderSaveReqVO createReqVO);

    /**
     * 更新Ozon订单
     *
     * @param updateReqVO 更新信息
     */
    void updateOzonOrder(@Valid OzonOrderSaveReqVO updateReqVO);

    /**
     * 删除Ozon订单
     *
     * @param id 编号
     */
    void deleteOzonOrder(Long id);

    /**
     * 获得Ozon订单
     *
     * @param id 编号
     * @return Ozon订单
     */
    OzonOrderDO getOzonOrder(Long id);

    /**
     * 获得Ozon订单分页
     *
     * @param pageReqVO 分页查询
     * @return Ozon订单分页
     */
    PageResult<OzonOrderDO> getOzonOrderPage(OzonOrderPageReqVO pageReqVO);

    /**
     * 获取Ozon订单分页
     *
     * @param page
     * @param reqVO
     * @return
     */
    IPage<OzonOrderDO> getOzonOrderPage2(IPage<OzonOrderDO> page, @Param("reqVO") OzonOrderPageReqVO reqVO);

    /**
     * 获取订单列表
     *
     * @param clientId
     * @param postingNumber
     * @param beginTime
     * @param endTime
     * @return
     */
    List<OzonOrderDO> getOzonOrderList(String clientId, String postingNumber, LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取订单列表
     *
     * @param clientId
     * @param platformOrderId
     * @param postingNumber
     * @param beginTime
     * @param endTime
     * @return
     */
    List<OzonOrderDO> getOzonOrderListByPlatformOrderId(String clientId, String platformOrderId, String postingNumber, LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取订单列表
     * @param clientIds
     * @param beginTime
     * @param endTime
     * @return
     */
    List<OzonOrderDO> getOzonOrderList(String[] clientIds,  LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取订单列表
     * @param clientId
     * @param platformOrderIds
     * @return
     */
    List<OzonOrderDO> getOrderList(String clientId, Collection<String> platformOrderIds);

    /**
     * 批量获取订单列表
     *
     * @param ids
     * @return
     */
    List<OzonOrderDO> batchOrderList(Collection<Long> ids);

    /**
     * 批量获取订单列表
     *
     * @param postingNumbers
     * @return
     */
    List<OzonOrderDO> batchOrderListByPostingNumbers(Collection<String> postingNumbers);

    /**
     * 批量获取订单
     *
     * @param clientIds
     * @param orderNumbers
     * @return
     */
    List<OzonOrderDO> batchOrderListByOrderNumbers(Collection<String> clientIds, Collection<String> orderNumbers);
    // ==================== 子表（订单商品详情） ====================

    /**
     * 获取订单商品详情列表
     *
     * @param orderId
     * @param postingNumber
     * @return
     */
    List<OzonOrderItemDO> getOrderItemListByOrderIdAndPostingNumber(String clientId, String orderId, String postingNumber);

    /**
     * 查询订单明细(这个方法慎用，offerId会变)
     * @param clientId
     * @param postingNumber
     * @return
     */
    List<OzonOrderItemDO> getOrderItemList(String clientId, String postingNumber, String offerId);

    /**
     * 获取订单商品详情列表
     *
     * @param clientId
     * @param postingNumber
     * @return
     */
    List<OzonOrderItemDO> getOrderItemListByPostingNumberAndPlatformSkuId(String clientId, String postingNumber, String platformSkuId);

    /**
     * 获取订单商品详情
     * @param clientIds
     * @param dateTimes
     * @return
     */
    List<OzonOrderItemDO> getOzonOrderItemListByClientId(String[] clientIds, LocalDateTime[] dateTimes);

    /**
     * 批量获取订单商品详情
     * @param postingNumbers
     * @return
     */
    List<OzonOrderItemDO> batchOrderItemListByPostingNumbers(String[] clientIds, Collection<String> postingNumbers);


    /**
     * 创建订单商品详情
     * @param orderId
     * @param items
     */
    void createOzonOrderItemList(String orderId, List<OzonOrderItemDO> items);

    /**
     * 更新订单商品详情
     * @param orderId
     * @param list
     */
    void updateOzonOrderItemList(String orderId, List<OzonOrderItemDO> list);

    /**
     * 获取订单商品详情分页
     * @param reqVO
     * @return
     */
    PageResult<OzonOrderItemDO> getOrderItemPage(ProductVolumeReqVO reqVO);

    /**
     * 获取订单商品详情分页,不含统计
     * @param reqVO
     * @return
     */
    PageResult<OzonOrderItemDO> getSimpleOrderItemPage(ProductVolumeReqVO reqVO);

    /**
     * 获取按门店维度统计的订单商品详情分页
     * @param reqVO
     * @return
     */
    PageResult<OzonOrderItemDO> getShopOrderItemPage(ProductVolumeReqVO reqVO);

    /**
     * 获取按SKU维度统计的订单商品详情分页
     * @param reqVO
     * @return
     */
    PageResult<OzonOrderItemDO> getSkuOrderItemPage(ProductVolumeReqVO reqVO);

    /**
     * 获取门店SKU维度信息
     *
     * @param reqVO
     * @return
     */
    List<ProductVolumeDTO> getSkuOrderItem(ProductVolumeReqVO reqVO);

    /**
     * 获取指定月份的销售数量
     *
     * @param clientIds 店铺编号列表
     * @param month 月份，格式为yyyy-MM
     * @param offerId 商品Offer ID
     * @return 销售数量Map，key为offerId，value为销售数量
     */
    Map<String, Integer> getMonthSalesQuantityMap(String[] clientIds, String month, String offerId);

    /**
     * 获取历史销售数量
     *
     * @param clientIds 店铺编号列表
     * @param month 月份，格式为yyyy-MM
     * @param offerId 商品Offer ID
     * @return 销售数量Map，key为offerId，value为销售数量
     */
    Map<String, Integer> getHistorySalesQuantityMap(String[] clientIds, String month, String offerId);

    /**
     * 通过发货编号和平台SKU ID删除订单商品明细
     *
     * @param clientId 平台门店id
     * @param postingNumber 发货编号
     * @param platformSkuId 平台SKU ID
     */
    void deleteOzonOrderItemByPostingNumberAndPlatformSkuId(String clientId, String postingNumber, String platformSkuId);
}