package cn.iocoder.yudao.module.dm.dal.mysql.ozonsupplyorder;

import java.util.*;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应订单商品 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface OzonSupplyOrderItemMapper extends BaseMapperX<OzonSupplyOrderItemDO> {

    default List<OzonSupplyOrderItemDO> selectListBySupplyOrderId(Long supplyOrderId) {
        return selectList(OzonSupplyOrderItemDO::getSupplyOrderId, supplyOrderId);
    }

    default int deleteBySupplyOrderId(Long supplyOrderId) {
        return delete(OzonSupplyOrderItemDO::getSupplyOrderId, supplyOrderId);
    }

}