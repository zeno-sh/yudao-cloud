package cn.iocoder.yudao.module.dm.dal.mysql.commission;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.commission.CategoryCommissionDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.commission.vo.*;
import org.apache.ibatis.annotations.Select;

/**
 * 类目佣金 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface CategoryCommissionMapper extends BaseMapperX<CategoryCommissionDO> {

    default List<CategoryCommissionDO> selectList(CategoryCommissionListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CategoryCommissionDO>()
                .likeIfPresent(CategoryCommissionDO::getCategoryName, reqVO.getCategoryName())
                .eqIfPresent(CategoryCommissionDO::getParentId, reqVO.getParentId())
                .orderByDesc(CategoryCommissionDO::getId));
    }

    default CategoryCommissionDO selectByParentIdAndCategoryName(Long parentId, String categoryName) {
        return selectOne(CategoryCommissionDO::getParentId, parentId, CategoryCommissionDO::getCategoryName, categoryName);
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(CategoryCommissionDO::getParentId, parentId);
    }


    @Select("SELECT id, category_name, rate, parent_id " +
            "FROM dm_category_commission " +
            "WHERE deleted = 0")
    List<CategoryCommissionDO> selectAllActiveCategories();
}