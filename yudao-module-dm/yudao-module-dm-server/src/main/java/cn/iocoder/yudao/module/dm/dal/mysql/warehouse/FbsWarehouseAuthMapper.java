package cn.iocoder.yudao.module.dm.dal.mysql.warehouse;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseAuthDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 海外仓授权信息 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface FbsWarehouseAuthMapper extends BaseMapperX<FbsWarehouseAuthDO> {

    default FbsWarehouseAuthDO selectByWarehouseId(Long warehouseId) {
        return selectOne(FbsWarehouseAuthDO::getWarehouseId, warehouseId);
    }

    default int deleteByWarehouseId(Long warehouseId) {
        return delete(FbsWarehouseAuthDO::getWarehouseId, warehouseId);
    }

}