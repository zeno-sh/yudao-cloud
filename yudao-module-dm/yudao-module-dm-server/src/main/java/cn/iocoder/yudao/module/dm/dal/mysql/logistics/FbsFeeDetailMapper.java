package cn.iocoder.yudao.module.dm.dal.mysql.logistics;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeDetailDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收费明细 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface FbsFeeDetailMapper extends BaseMapperX<FbsFeeDetailDO> {

    default List<FbsFeeDetailDO> selectListByServiceId(Long serviceId) {
        return selectList(FbsFeeDetailDO::getServiceId, serviceId);
    }

    default int deleteByServiceId(Long serviceId) {
        return delete(FbsFeeDetailDO::getServiceId, serviceId);
    }

}