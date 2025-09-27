package cn.iocoder.yudao.module.dm.service.plan;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.plan.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.plan.ProductSelectionPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 选品计划 Service 接口
 *
 * @author Zeno
 */
public interface ProductSelectionPlanService {

    /**
     * 创建选品计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductSelectionPlan(@Valid ProductSelectionPlanSaveReqVO createReqVO);

    /**
     * 更新选品计划
     *
     * @param updateReqVO 更新信息
     */
    void updateProductSelectionPlan(@Valid ProductSelectionPlanSaveReqVO updateReqVO);

    /**
     * 删除选品计划
     *
     * @param id 编号
     */
    void deleteProductSelectionPlan(Long id);

    /**
     * 获得选品计划
     *
     * @param id 编号
     * @return 选品计划
     */
    ProductSelectionPlanDO getProductSelectionPlan(Long id);

    /**
     * 获得选品计划分页
     *
     * @param pageReqVO 分页查询
     * @return 选品计划分页
     */
    PageResult<ProductSelectionPlanDO> getProductSelectionPlanPage(ProductSelectionPlanPageReqVO pageReqVO);

    /**
     * 根据productId获取详情
     *
     * @param productId
     * @return
     */
    List<ProductSelectionPlanDO> getProductSelectionPlanByProductId(Long productId);

    // ==================== 子表（采购信息） ====================

    /**
     * 获得采购信息列表
     *
     * @param productId 产品Id
     * @return 采购信息列表
     */
    List<ProductPurchaseDO> getProductPurchaseListByProductId(Long productId);

}