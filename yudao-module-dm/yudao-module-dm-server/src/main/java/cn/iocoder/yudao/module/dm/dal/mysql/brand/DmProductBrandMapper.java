package cn.iocoder.yudao.module.dm.dal.mysql.brand;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.brand.DmProductBrandDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.brand.vo.*;

/**
 * 品牌信息 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface DmProductBrandMapper extends BaseMapperX<DmProductBrandDO> {

    default PageResult<DmProductBrandDO> selectPage(DmProductBrandPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DmProductBrandDO>()
                .likeIfPresent(DmProductBrandDO::getName, reqVO.getName())
                .betweenIfPresent(DmProductBrandDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DmProductBrandDO::getId));
    }

}