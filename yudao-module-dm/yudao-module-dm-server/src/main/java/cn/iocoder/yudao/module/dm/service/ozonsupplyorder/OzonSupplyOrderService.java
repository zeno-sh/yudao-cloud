package cn.iocoder.yudao.module.dm.service.ozonsupplyorder;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderStatsDO;

/**
 * 供应订单 Service 接口
 *
 * @author Zeno
 */
public interface OzonSupplyOrderService {

    /**
     * 创建供应订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOzonSupplyOrder(@Valid OzonSupplyOrderSaveReqVO createReqVO);

    /**
     * 更新供应订单
     *
     * @param updateReqVO 更新信息
     */
    void updateOzonSupplyOrder(@Valid OzonSupplyOrderSaveReqVO updateReqVO);

    /**
     * 删除供应订单
     *
     * @param id 编号
     */
    void deleteOzonSupplyOrder(Long id);

    /**
     * 获得供应订单
     *
     * @param id 编号
     * @return 供应订单
     */
    OzonSupplyOrderDO getOzonSupplyOrder(Long id);

    /**
     * 获得供应订单分页
     *
     * @param pageReqVO 分页查询
     * @return 供应订单分页
     */
    PageResult<OzonSupplyOrderDO> getOzonSupplyOrderPage(OzonSupplyOrderPageReqVO pageReqVO);

    /**
     * 获得供应订单商品列表
     *
     * @param supplyOrderId 供应订单编号
     * @return 供应订单商品列表
     */
    List<OzonSupplyOrderItemDO> getOzonSupplyOrderItemListBySupplyOrderId(Long supplyOrderId);

    /**
     * 获取供应订单统计信息
     *
     * @param clientId 客户端编号
     * @param supplyOrderIds 供应订单编号列表
     * @return 供应订单统计信息列表
     */
    List<OzonSupplyOrderStatsDO> getOzonSupplyOrderStats(String clientId, Collection<Long> supplyOrderIds);

    /**
     * 同步指定供应订单的商品信息
     *
     * @param supplyOrderId 供应订单ID
     */
    void syncSupplyOrderItems(Long supplyOrderId);

    /**
     * 获取FBO进仓报表分页数据
     *
     * @param reqVO 请求参数
     * @return FBO进仓报表分页数据
     */
    PageResult<OzonFboInboundReportRespVO> getFboInboundReportPage(OzonFboInboundReportReqVO reqVO);

}