package cn.iocoder.yudao.module.dm.dal.mysql.warehouse;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsPushOrderLogDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;

/**
 * 海外仓推单记录 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface FbsPushOrderLogMapper extends BaseMapperX<FbsPushOrderLogDO> {

    default PageResult<FbsPushOrderLogDO> selectPage(FbsPushOrderLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FbsPushOrderLogDO>()
                .eqIfPresent(FbsPushOrderLogDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(FbsPushOrderLogDO::getPlatformOrderId, reqVO.getPlatformOrderId())
                .eqIfPresent(FbsPushOrderLogDO::getRequest, reqVO.getRequest())
                .eqIfPresent(FbsPushOrderLogDO::getResponse, reqVO.getResponse())
                .eqIfPresent(FbsPushOrderLogDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(FbsPushOrderLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FbsPushOrderLogDO::getId));
    }

}