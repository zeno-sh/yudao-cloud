package cn.iocoder.yudao.module.dm.dal.mysql.purchaseorderlog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 采购单到货日志 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface PurchaseOrderArrivedLogMapper extends BaseMapperX<PurchaseOrderArrivedLogDO> {

    default PageResult<PurchaseOrderArrivedLogDO> selectPage(PurchaseOrderArrivedLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PurchaseOrderArrivedLogDO>()
                .eqIfPresent(PurchaseOrderArrivedLogDO::getPurchaseOrderId, reqVO.getPurchaseOrderId())
                .eqIfPresent(PurchaseOrderArrivedLogDO::getPurchaseOrderItemId, reqVO.getPurchaseOrderItemId())
                .eqIfPresent(PurchaseOrderArrivedLogDO::getArrivedQuantity, reqVO.getArrivedQuantity())
                .eqIfPresent(PurchaseOrderArrivedLogDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(PurchaseOrderArrivedLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PurchaseOrderArrivedLogDO::getId));
    }

    default List<PurchaseOrderArrivedLogDO> selectListByPurchaseOrderId(Long purchaseOrderId) {
        return selectList(PurchaseOrderArrivedLogDO::getPurchaseOrderId, purchaseOrderId);
    }

    default int deleteByPurchaseOrderId(Long purchaseOrderId) {
        return delete(PurchaseOrderArrivedLogDO::getPurchaseOrderId, purchaseOrderId);
    }

}