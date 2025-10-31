package cn.iocoder.yudao.module.dm.service.warehouse;

import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsProductStockDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 海外仓产品库存 Service 接口
 *
 * @author Zeno
 */
public interface FbsProductStockService {

    /**
     * 创建海外仓产品库存
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFbsProductStock(@Valid FbsProductStockSaveReqVO createReqVO);

    /**
     * 更新海外仓产品库存
     *
     * @param updateReqVO 更新信息
     */
    void updateFbsProductStock(@Valid FbsProductStockSaveReqVO updateReqVO);

    /**
     * 删除海外仓产品库存
     *
     * @param id 编号
     */
    void deleteFbsProductStock(Long id);

    /**
     * 获得海外仓产品库存
     *
     * @param id 编号
     * @return 海外仓产品库存
     */
    FbsProductStockDO getFbsProductStock(Long id);

    /**
     * 根据仓库id和商品id获得海外仓产品库存
     *
     * @param warehouseId
     * @param productId
     * @return
     */
    FbsProductStockDO getFbsProductStockByWarehouseIdAndProductId(Long warehouseId, Long productId);

    /**
     * 获得海外仓产品库存分页
     *
     * @param pageReqVO 分页查询
     * @return 海外仓产品库存分页
     */
    PageResult<FbsProductStockDO> getFbsProductStockPage(FbsProductStockPageReqVO pageReqVO);

}