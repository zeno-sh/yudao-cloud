package cn.iocoder.yudao.module.dm.service.dmcategory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.category.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.dmcategory.DmProductCategoryDO;

/**
 * 产品分类 Service 接口
 *
 * @author Zeno
 */
public interface DmProductCategoryService {

    /**
     * 创建产品分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductCategory(@Valid DmProductCategorySaveReqVO createReqVO);

    /**
     * 更新产品分类
     *
     * @param updateReqVO 更新信息
     */
    void updateProductCategory(@Valid DmProductCategorySaveReqVO updateReqVO);

    /**
     * 删除产品分类
     *
     * @param id 编号
     */
    void deleteProductCategory(Long id);

    /**
     * 获得产品分类
     *
     * @param id 编号
     * @return 产品分类
     */
    DmProductCategoryDO getProductCategory(Long id);

    /**
     * 获得产品分类列表
     *
     * @param listReqVO 查询条件
     * @return 产品分类列表
     */
    List<DmProductCategoryDO> getProductCategoryList(DmProductCategoryListReqVO listReqVO);

}