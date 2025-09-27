package cn.iocoder.yudao.module.dm.dal.mysql.warehouse;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;

/**
 * 海外仓仓库 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface FbsWarehouseMapper extends BaseMapperX<FbsWarehouseDO> {

    default PageResult<FbsWarehouseDO> selectPage(FbsWarehousePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FbsWarehouseDO>()
                .likeIfPresent(FbsWarehouseDO::getName, reqVO.getName())
                .orderByDesc(FbsWarehouseDO::getId));
    }

}