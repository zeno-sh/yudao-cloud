package cn.iocoder.yudao.module.dm.dal.mysql.transport;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 头程计划明细 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface TransportPlanItemMapper extends BaseMapperX<TransportPlanItemDO> {

    /**
     * 批量查询指定产品ID和时间范围内的头程计划明细
     *
     * @param productIds 产品ID列表
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 头程计划明细列表
     */
    List<TransportPlanItemDO> selectListByProductIdsAndDateRange(@Param("productIds") Collection<Long> productIds,
                                                                @Param("beginTime") LocalDateTime beginTime,
                                                                @Param("endTime") LocalDateTime endTime);

    default List<TransportPlanItemDO> selectListByPlanId(Long planId) {
        return selectList(TransportPlanItemDO::getPlanId, planId);
    }

    default int deleteByPlanId(Long planId) {
        return delete(TransportPlanItemDO::getPlanId, planId);
    }

}