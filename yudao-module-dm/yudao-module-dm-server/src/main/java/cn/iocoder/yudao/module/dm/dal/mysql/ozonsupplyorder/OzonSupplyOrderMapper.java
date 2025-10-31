package cn.iocoder.yudao.module.dm.dal.mysql.ozonsupplyorder;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonFboInboundStatsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonFboSalesStatsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderStatsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 供应订单 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface OzonSupplyOrderMapper extends BaseMapperX<OzonSupplyOrderDO> {

    default PageResult<OzonSupplyOrderDO> selectPage(OzonSupplyOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OzonSupplyOrderDO>()
                .inIfPresent(OzonSupplyOrderDO::getClientId, Arrays.asList(reqVO.getClientIds()))
                .eqIfPresent(OzonSupplyOrderDO::getSupplyOrderId, reqVO.getSupplyOrderId())
                .eqIfPresent(OzonSupplyOrderDO::getState, reqVO.getState())
                .eqIfPresent(OzonSupplyOrderDO::getWarehouseId, reqVO.getWarehouseId())
                .likeIfPresent(OzonSupplyOrderDO::getWarehouseName, reqVO.getWarehouseName())
                .betweenIfPresent(OzonSupplyOrderDO::getTimeslotFrom, reqVO.getTimeslotFrom(), reqVO.getTimeslotTo())
                .betweenIfPresent(OzonSupplyOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OzonSupplyOrderDO::getCreationDate));
    }

    /**
     * 批量查询订单统计信息
     *
     * @param clientId 客户端编号
     * @param supplyOrderIds 供应订单编号列表
     * @return 订单统计信息列表
     */
    List<OzonSupplyOrderStatsDO> selectOrderStatsBatch(@Param("clientId") String clientId,
                                                       @Param("supplyOrderIds") Collection<Long> supplyOrderIds);

    /**
     * 分页查询FBO进仓报表数据
     *
     * @param page 分页参数
     * @param reqVO 查询条件
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return FBO进仓统计数据列表
     */
    IPage<OzonFboInboundStatsDO> selectFboInboundPage(IPage<OzonFboInboundStatsDO> page,
                                                      @Param("reqVO") OzonFboInboundReportReqVO reqVO,
                                                      @Param("beginTime") Date beginTime,
                                                      @Param("endTime") Date endTime);

    /**
     * 获取FBO销售统计数据
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param clientIds 客户端编号列表
     * @param dmProductIds 本地产品ID
     * @return FBO销售统计数据列表
     */
    List<OzonFboSalesStatsDO> selectFboSalesStats(@Param("beginTime") Date beginTime,
                                                  @Param("endTime") Date endTime,
                                                  @Param("clientIds") String[] clientIds,
                                                  @Param("dmProductIds") List<Long> dmProductIds);

    /**
     * 获取FBO历史进仓统计数据
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param clientIds 客户端编号列表
     * @param dmProductIds SKU列表
     * @return FBO进仓统计数据列表
     */
    List<OzonFboInboundStatsDO> selectFboInboundStats(@Param("beginTime") Date beginTime,
                                                      @Param("endTime") Date endTime,
                                                      @Param("clientIds") String[] clientIds,
                                                      @Param("dmProductIds") List<Long> dmProductIds);

}