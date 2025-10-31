package cn.iocoder.yudao.module.dm.dal.mysql.logistics;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeServicesDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.logistics.vo.*;

/**
 * 收费项目 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface FbsFeeServicesMapper extends BaseMapperX<FbsFeeServicesDO> {

    default PageResult<FbsFeeServicesDO> selectPage(FbsFeeServicesPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FbsFeeServicesDO>()
                .eqIfPresent(FbsFeeServicesDO::getWarehouseId, reqVO.getWarehouseId())
                .likeIfPresent(FbsFeeServicesDO::getName, reqVO.getName())
                .orderByDesc(FbsFeeServicesDO::getId));
    }

}