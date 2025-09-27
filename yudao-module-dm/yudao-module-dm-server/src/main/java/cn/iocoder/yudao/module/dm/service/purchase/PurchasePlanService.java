package cn.iocoder.yudao.module.dm.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.PurchasePlanImportVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.PurchasePlanPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.PurchasePlanSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 采购计划 Service 接口
 *
 * @author Zeno
 */
public interface PurchasePlanService {

    /**
     * 创建采购计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchasePlan(@Valid PurchasePlanSaveReqVO createReqVO);

    /**
     * 采购计划导入
     *
     * @param purchasePlanImportVOList
     * @return
     */
    Long importPurchasePlan(List<PurchasePlanImportVO> purchasePlanImportVOList);

    /**
     * 更新采购计划
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchasePlan(@Valid PurchasePlanSaveReqVO updateReqVO);

    /**
     * 删除采购计划
     *
     * @param id 编号
     */
    void deletePurchasePlan(Long id);

    /**
     * 获得采购计划
     *
     * @param id 编号
     * @return 采购计划
     */
    PurchasePlanDO getPurchasePlan(Long id);

    /**
     * 批量查询采购计划
     *
     * @param ids
     * @return
     */
    List<PurchasePlanDO> batchQueryPurchasePlan(Collection<Long> ids);

    /**
     * 批量查询采购计划
     *
     * @param batchNumbers
     * @return
     */
    List<PurchasePlanDO> batchQueryPurchasePlanByNumber(Collection<String> batchNumbers);

    /**
     * 获得采购计划分页
     *
     * @param pageReqVO 分页查询
     * @return 采购计划分页
     */
    PageResult<PurchasePlanDO> getPurchasePlanPage(PurchasePlanPageReqVO pageReqVO);

    // ==================== 子表（采购计划详情） ====================

    /**
     * 获取采购计划详情
     *
     * @param planNumber
     * @return
     */
    PurchasePlanItemDO getPurchasePlanItem(String planNumber);

    /**
     * 获得采购计划详情列表
     *
     * @param planId 计划id
     * @return 采购计划详情列表
     */
    List<PurchasePlanItemDO> getPurchasePlanItemListByPlanId(Long planId);

    /**
     * 批量获取采购计划明细
     *
     * @param planIds
     * @return
     */
    List<PurchasePlanItemDO> batchPurchasePlanItemListByPlanIds(Collection<Long> planIds);

    /**
     * 批量获取采购计划明细
     *
     * @param planNumbers
     * @return
     */
    List<PurchasePlanItemDO> batchPurchasePlanItemListByPlanNumbers(Collection<String> planNumbers);

    /**
     * 更新采购计划状态
     *
     * @param planId
     * @param status
     */
    void updatePurchasePlanAuditStatus(Long planId, Integer status);

    /**
     * 提交审核
     *
     * @param planId
     * @return
     */
    void submitReview(Long planId);

    /**
     * 更新采购计划明细状态
     *
     * @param planNumber
     * @param status
     */
    void updatePurchasePlanItemStatus(String planNumber, Integer status);
}