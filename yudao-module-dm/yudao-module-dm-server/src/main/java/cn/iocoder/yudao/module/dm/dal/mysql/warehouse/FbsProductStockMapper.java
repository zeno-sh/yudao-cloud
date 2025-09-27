package cn.iocoder.yudao.module.dm.dal.mysql.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsProductStockDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;

/**
 * 海外仓产品库存 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface FbsProductStockMapper extends BaseMapperX<FbsProductStockDO> {

    default PageResult<FbsProductStockDO> selectPage(FbsProductStockPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FbsProductStockDO>()
                .eqIfPresent(FbsProductStockDO::getWarehouseId, reqVO.getWarehouseId())
                .likeIfPresent(FbsProductStockDO::getProductSku, reqVO.getProductSku())
                .eqIfPresent(FbsProductStockDO::getProductId, reqVO.getProductId())
                .orderByDesc(FbsProductStockDO::getSellable));
    }

}