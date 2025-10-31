package cn.iocoder.yudao.module.dm.dal.mysql.dmcategory;

import java.util.*;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.dmcategory.DmProductCategoryDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.category.vo.*;

/**
 * 产品分类 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface DmProductCategoryMapper extends BaseMapperX<DmProductCategoryDO> {

    default List<DmProductCategoryDO> selectList(DmProductCategoryListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DmProductCategoryDO>()
                .likeIfPresent(DmProductCategoryDO::getName, reqVO.getName())
                .eqIfPresent(DmProductCategoryDO::getCode, reqVO.getCode())
                .orderByDesc(DmProductCategoryDO::getId));
    }

	default DmProductCategoryDO selectByParentIdAndName(Long parentId, String name) {
	    return selectOne(DmProductCategoryDO::getParentId, parentId, DmProductCategoryDO::getName, name);
	}

    default Long selectCountByParentId(Long parentId) {
        return selectCount(DmProductCategoryDO::getParentId, parentId);
    }

}