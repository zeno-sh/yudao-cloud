package cn.iocoder.yudao.module.dm.service.productcosts;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.productcosts.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 产品成本结构 Service 接口
 *
 * @author Zeno
 */
public interface ProductCostsService {

    /**
     * 创建产品成本结构
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductCosts(@Valid ProductCostsSaveReqVO createReqVO);

    /**
     * 更新产品成本结构
     *
     * @param updateReqVO 更新信息
     */
    void updateProductCosts(@Valid ProductCostsSaveReqVO updateReqVO);

    /**
     * 删除产品成本结构
     *
     * @param id 编号
     */
    void deleteProductCosts(Long id);

    /**
     * 根据产品编号删除产品成本结构
     *
     * @param productId
     */
    void deleteProductCostsByProductId(Long productId);

    /**
     * 获得产品成本结构
     *
     * @param id 编号
     * @return 产品成本结构
     */
    ProductCostsDO getProductCosts(Long id);

    /**
     * 根据产品ID和平台获得产品成本结构
     *
     * @param productId 产品ID
     * @param platform 目标平台
     * @return 产品成本结构
     */
    ProductCostsDO getProductCostsByProductIdAndPlatform(Long productId, Integer platform);

    /**
     * 获得产品成本结构分页
     *
     * @param pageReqVO 分页查询
     * @return 产品成本结构分页
     */
    PageResult<ProductCostsDO> getProductCostsPage(ProductCostsPageReqVO pageReqVO);

    /**
     * 获得产品成本结构列表
     *
     * @param productId
     * @return
     */
    List<ProductCostsDO> getProductCostsListByProductId(Long productId);

    /**
     * 批量获得产品成本结构列表
     *
     * @param productIds
     * @return
     */
    List<ProductCostsDO> batchProductCostsListByProductIds(Collection<Long> productIds);
}