package cn.iocoder.yudao.module.dm.service.supplier;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.dm.controller.admin.supplier.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.supplier.ProductSupplierDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 供应商信息 Service 接口
 *
 * @author Zeno
 */
public interface ProductSupplierService {

    /**
     * 创建供应商信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductSupplier(@Valid ProductSupplierSaveReqVO createReqVO);

    /**
     * 更新供应商信息
     *
     * @param updateReqVO 更新信息
     */
    void updateProductSupplier(@Valid ProductSupplierSaveReqVO updateReqVO);

    /**
     * 删除供应商信息
     *
     * @param id 编号
     */
    void deleteProductSupplier(Long id);

    /**
     * 获得供应商信息
     *
     * @param id 编号
     * @return 供应商信息
     */
    ProductSupplierDO getProductSupplier(Long id);

    /**
     * 获得供应商信息分页
     *
     * @param pageReqVO 分页查询
     * @return 供应商信息分页
     */
    PageResult<ProductSupplierDO> getProductSupplierPage(ProductSupplierPageReqVO pageReqVO);

    /**
     * 批量查询供应商信息
     *
     * @param ids
     * @return
     */
    List<ProductSupplierDO> batchQueryProductSupplierByIds(Collection<Long> ids);

    /**
     * 查询供应商
     *
     * @param ids
     * @return
     */
    Map<Long, ProductSupplierDO> getSupplierMap(Collection<Long> ids);
}