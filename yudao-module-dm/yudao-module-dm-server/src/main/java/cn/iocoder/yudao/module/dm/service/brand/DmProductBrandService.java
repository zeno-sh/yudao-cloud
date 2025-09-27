package cn.iocoder.yudao.module.dm.service.brand;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.brand.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.brand.DmProductBrandDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 品牌信息 Service 接口
 *
 * @author Zeno
 */
public interface DmProductBrandService {

    /**
     * 创建品牌信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductBrand(@Valid DmProductBrandSaveReqVO createReqVO);

    /**
     * 更新品牌信息
     *
     * @param updateReqVO 更新信息
     */
    void updateProductBrand(@Valid DmProductBrandSaveReqVO updateReqVO);

    /**
     * 删除品牌信息
     *
     * @param id 编号
     */
    void deleteProductBrand(Long id);

    /**
     * 获得品牌信息
     *
     * @param id 编号
     * @return 品牌信息
     */
    DmProductBrandDO getProductBrand(Long id);

    /**
     * 获得品牌信息分页
     *
     * @param pageReqVO 分页查询
     * @return 品牌信息分页
     */
    PageResult<DmProductBrandDO> getProductBrandPage(DmProductBrandPageReqVO pageReqVO);

}