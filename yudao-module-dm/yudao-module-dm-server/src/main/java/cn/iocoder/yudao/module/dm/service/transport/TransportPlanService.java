package cn.iocoder.yudao.module.dm.service.transport;

import java.util.*;
import javax.validation.*;
import java.time.LocalDateTime;

import cn.iocoder.yudao.module.dm.controller.admin.transport.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanDetailDTO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 头程计划 Service 接口
 *
 * @author Zeno
 */
public interface TransportPlanService {

    /**
     * 创建头程计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTransportPlan(@Valid TransportPlanSaveReqVO createReqVO);

    /**
     * 更新头程计划
     *
     * @param updateReqVO 更新信息
     */
    void updateTransportPlan(@Valid TransportPlanSaveReqVO updateReqVO);

    /**
     * 删除头程计划
     *
     * @param id 编号
     */
    void deleteTransportPlan(Long id);

    /**
     * 获得头程计划
     *
     * @param id 编号
     * @return 头程计划
     */
    TransportPlanDO getTransportPlan(Long id);

    /**
     * 获得头程计划分页
     *
     * @param pageReqVO 分页查询
     * @return 头程计划分页
     */
    PageResult<TransportPlanDO> getTransportPlanPage(TransportPlanPageReqVO pageReqVO);

    // ==================== 子表（头程计划明细） ====================

    /**
     * 获得头程计划明细列表
     *
     * @param planId 头程计划ID
     * @return 头程计划明细列表
     */
    List<TransportPlanItemDO> getTransportPlanItemListByPlanId(Long planId);

    /**
     * 根据 productId 获取明细列表
     *
     * @param productId
     * @return
     */
    List<TransportPlanItemDO> getTransportPlanItemListByProductId(Long productId);

    /**
     * 批量头程计划明细列表
     *
     * @param purchaseOrderItemIds
     * @return
     */
    List<TransportPlanItemDO> batchTransportPlanItemListByPurchaseOrderItemIds(Collection<Long> purchaseOrderItemIds);

    /**
     * 获取头程明细
     *
     * @param purchaseOrderItemId
     * @return
     */
    TransportPlanItemDO getTransportPlanItemByPurchaseOrderItemId(Long purchaseOrderItemId);


    /**
     * 获取头程明细
     *
     * @param purchaseOrderItemIds
     * @return
     */
    List<TransportPlanItemDO> batchTransportPlanItemByPurchaseOrderItemId(Collection<Long> purchaseOrderItemIds);

    /**
     * 批量查询指定产品ID和时间范围内的头程计划明细
     *
     * @param productIds 产品ID列表
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 头程计划明细列表
     */
    List<TransportPlanItemDO> getTransportPlanItemListByProductIdsAndDateRange(Collection<Long> productIds,
                                                                              LocalDateTime beginTime,
                                                                              LocalDateTime endTime);

    /**
     * 获得头程计划明细列表
     *
     * @param reqVO 查询条件
     * @return 头程计划明细列表
     */
    List<TransportPlanDetailDTO> getTransportPlanDetailList(TransportPlanDetailReqVO reqVO);
}