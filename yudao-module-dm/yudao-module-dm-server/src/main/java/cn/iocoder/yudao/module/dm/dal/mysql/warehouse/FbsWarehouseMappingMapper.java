package cn.iocoder.yudao.module.dm.dal.mysql.warehouse;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseMappingDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 海外仓平台仓映射 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface FbsWarehouseMappingMapper extends BaseMapperX<FbsWarehouseMappingDO> {

    default List<FbsWarehouseMappingDO> selectListByWarehouseId(Long warehouseId) {
        return selectList(FbsWarehouseMappingDO::getWarehouseId, warehouseId);
    }

    default int deleteByWarehouseId(Long warehouseId) {
        return delete(FbsWarehouseMappingDO::getWarehouseId, warehouseId);
    }

}