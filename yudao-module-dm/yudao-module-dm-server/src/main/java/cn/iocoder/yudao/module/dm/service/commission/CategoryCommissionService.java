package cn.iocoder.yudao.module.dm.service.commission;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.commission.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.commission.CategoryCommissionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 类目佣金 Service 接口
 *
 * @author Zeno
 */
public interface CategoryCommissionService {

    /**
     * 创建类目佣金
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategoryCommission(@Valid CategoryCommissionSaveReqVO createReqVO);

    /**
     * 更新类目佣金
     *
     * @param updateReqVO 更新信息
     */
    void updateCategoryCommission(@Valid CategoryCommissionSaveReqVO updateReqVO);

    /**
     * 删除类目佣金
     *
     * @param id 编号
     */
    void deleteCategoryCommission(Long id);

    /**
     * 获得类目佣金
     *
     * @param id 编号
     * @return 类目佣金
     */
    CategoryCommissionDO getCategoryCommission(Long id);

    /**
     * 获得类目佣金列表
     *
     * @param listReqVO 查询条件
     * @return 类目佣金列表
     */
    List<CategoryCommissionDO> getCategoryCommissionList(CategoryCommissionListReqVO listReqVO);

    List<CategoryCommissionTreeRespVO> getCategoryCommissionTree();

    List<CategoryCommissionDO> batchQueryByIds(List<Long> ids);
}