package cn.iocoder.yudao.module.dm.dal.mysql.purchaseorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.ReportStockReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.enums.PurchaseOrderStatusEnum;
import cn.iocoder.yudao.module.dm.service.purchase.order.dto.ProductStockDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.Arrays;
import java.util.List;

/**
 * 采购单 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface DmPurchaseOrderMapper extends BaseMapperX<PurchaseOrderDO> {

    default PageResult<PurchaseOrderDO> selectPage(PurchaseOrderPageReqVO reqVO) {
        LambdaQueryWrapperX<PurchaseOrderDO> query = new LambdaQueryWrapperX<PurchaseOrderDO>()
                .eqIfPresent(PurchaseOrderDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(PurchaseOrderDO::getOwner, reqVO.getOwner())
                .eqIfPresent(PurchaseOrderDO::getSettleType, reqVO.getSettleType())
                .eqIfPresent(PurchaseOrderDO::getTax, reqVO.getTax())
                .eqIfPresent(PurchaseOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PurchaseOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PurchaseOrderDO::getId);

        if (Boolean.TRUE.equals(reqVO.getPaymentEnable())) {
            query.in(PurchaseOrderDO::getStatus, Arrays.asList(PurchaseOrderStatusEnum.DO_ORDER.getStatus(),
                            PurchaseOrderStatusEnum.DO_ARRIVE.getStatus(),
                            PurchaseOrderStatusEnum.SUCCESS.getStatus()))
                    .apply("payment_price < total_price");
        }
        return selectPage(reqVO, query);
    }

    IPage<PurchaseOrderDO> selectPage2(IPage<PurchaseOrderDO> page, @Param("reqVO") PurchaseOrderPageReqVO reqVO);

    /**
     * 按月统计产品库存（带分页）
     */
    IPage<ProductStockDTO> selectMonthlyProductStock(IPage<ProductStockDTO> page, @Param("reqVO") ReportStockReqVO reqVO);

}