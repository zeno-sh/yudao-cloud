package cn.iocoder.yudao.module.dm.dal.mysql.transport;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.TransportReportVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanDetailDTO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.transport.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 头程计划 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface TransportPlanMapper extends BaseMapperX<TransportPlanDO> {

    default PageResult<TransportPlanDO> selectPage(TransportPlanPageReqVO reqVO) {
        LambdaQueryWrapperX<TransportPlanDO> queryWrapper = new LambdaQueryWrapperX<TransportPlanDO>()
                .eqIfPresent(TransportPlanDO::getCode, reqVO.getCode())
                .eqIfPresent(TransportPlanDO::getTransportStatus, reqVO.getTransportStatus())
                .likeIfPresent(TransportPlanDO::getOverseaLocationCheckinId, reqVO.getOverseaLocationCheckinId())
                .eqIfPresent(TransportPlanDO::getForwarder, reqVO.getForwarder())
                .eqIfPresent(TransportPlanDO::getOfferPrice, reqVO.getOfferPrice())
                .eqIfPresent(TransportPlanDO::getCurrency, reqVO.getCurrency())
                .eqIfPresent(TransportPlanDO::getSettleStatus, reqVO.getSettleStatus())
                .eqIfPresent(TransportPlanDO::getBillPrice, reqVO.getBillPrice())
                .eqIfPresent(TransportPlanDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(TransportPlanDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(TransportPlanDO::getDespatchDate, reqVO.getDespatchDate())
                .betweenIfPresent(TransportPlanDO::getArrivalDate, reqVO.getArrivalDate())
                .orderByDesc(TransportPlanDO::getId);
        
        if (reqVO.getProductId() != null) {
            queryWrapper.inSql(TransportPlanDO::getId, 
                "SELECT plan_id FROM dm_transport_plan_item WHERE product_id = " + reqVO.getProductId() + " AND deleted = 0");
        }
        
        return selectPage(reqVO, queryWrapper);
    }

    /**
     * 查询月度发货计划报表
     *
     * @param page 分页参数
     * @param startDate 开始日期，格式：yyyy-MM-dd HH:mm:ss
     * @param endDate 结束日期，格式：yyyy-MM-dd HH:mm:ss
     * @param productId 产品编号
     * @return 月度发货计划报表分页
     */
    IPage<TransportReportVO> selectMonthlyTransportReport(IPage<TransportReportVO> page,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate,
                                                         @Param("productId") Long productId);

    /**
     * 获得头程计划明细列表
     *
     * @param reqVO 查询条件
     * @return 头程计划明细列表
     */
    List<TransportPlanDetailDTO> selectTransportPlanDetailList(@Param("reqVO") TransportPlanDetailReqVO reqVO);
}