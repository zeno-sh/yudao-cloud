package cn.iocoder.yudao.module.dm.service.purchase.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.PurchasePlanItemPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;

import java.util.Collection;
import java.util.List;

/**
 * 采购计划详情 Service 接口
 *
 * @author Zeno
 */
public interface PurchasePlanItemService {


    /**
     * 获得采购计划详情
     *
     * @param id 编号
     * @return 采购计划详情
     */
    PurchasePlanItemDO getPurchasePlanItem(Long id);

    /**
     * 获得采购计划详情
     *
     * @param planNumber
     * @return
     */
    PurchasePlanItemDO getPurchasePlanItemByPlanNumber(String planNumber);

    /**
     * 根据productId获取详情
     *
     * @param productId
     * @return
     */
    List<PurchasePlanItemDO> getPurchasePlanItemByProductId(Long productId);

    /**
     * 批量获取详情
     *
     * @param productIds
     * @return
     */
    List<PurchasePlanItemDO> batchPurchasePlanItem(Collection<Long> productIds, Collection<String> planNumbers,
                                                   Collection<Long> userId);


    /**
     * 获得采购计划详情分页
     *
     * @param pageReqVO 分页查询
     * @return 采购计划详情分页
     */
    PageResult<PurchasePlanItemDO> getPurchasePlanItemPage(PurchasePlanItemPageReqVO pageReqVO);

}