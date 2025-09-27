package cn.iocoder.yudao.module.dm.service.warehouse;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsProductStockDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.warehouse.FbsProductStockMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 海外仓产品库存 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class FbsProductStockServiceImpl implements FbsProductStockService {

    @Resource
    private FbsProductStockMapper fbsProductStockMapper;

    @Override
    public Long createFbsProductStock(FbsProductStockSaveReqVO createReqVO) {
        // 插入
        String productSku = createReqVO.getProductSku();
        FbsProductStockDO existDO = fbsProductStockMapper.selectOne(FbsProductStockDO::getProductSku, productSku,
                FbsProductStockDO::getWarehouseId, createReqVO.getWarehouseId());
        if (existDO != null) {
            createReqVO.setId(existDO.getId());
            updateFbsProductStock(createReqVO);
            return existDO.getId();
        }

        FbsProductStockDO fbsProductStock = BeanUtils.toBean(createReqVO, FbsProductStockDO.class);
        fbsProductStockMapper.insert(fbsProductStock);
        // 返回
        return fbsProductStock.getId();
    }

    @Override
    public void updateFbsProductStock(FbsProductStockSaveReqVO updateReqVO) {
        // 校验存在
        validateFbsProductStockExists(updateReqVO.getId());
        validateDmProductIdExists(updateReqVO.getWarehouseId(), updateReqVO.getProductId());
        // 更新
        FbsProductStockDO updateObj = BeanUtils.toBean(updateReqVO, FbsProductStockDO.class);
        fbsProductStockMapper.updateById(updateObj);
    }

    @Override
    public void deleteFbsProductStock(Long id) {
        // 校验存在
        validateFbsProductStockExists(id);
        // 删除
        fbsProductStockMapper.deleteById(id);
    }

    private void validateFbsProductStockExists(Long id) {
        if (fbsProductStockMapper.selectById(id) == null) {
            throw exception(FBS_PRODUCT_STOCK_NOT_EXISTS);
        }
    }

    private void validateDmProductIdExists(Long warehouseId, Long dmProductId) {
        if (null == dmProductId) {
            return;
        }
        LambdaQueryWrapperX<FbsProductStockDO> queryWrapperX = new LambdaQueryWrapperX<FbsProductStockDO>()
                .eq(FbsProductStockDO::getWarehouseId, warehouseId)
                .eq(FbsProductStockDO::getProductId, dmProductId);
        if (fbsProductStockMapper.selectCount(queryWrapperX) > 0) {
            throw exception(FBS_WAREHOUSE_PRODUCT_MAPPING_EXISTS);
        }
    }

    @Override
    public FbsProductStockDO getFbsProductStock(Long id) {
        return fbsProductStockMapper.selectById(id);
    }

    @Override
    public FbsProductStockDO getFbsProductStockByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        return fbsProductStockMapper.selectOne(FbsProductStockDO::getWarehouseId, warehouseId,FbsProductStockDO::getProductId, productId);
    }

    @Override
    public PageResult<FbsProductStockDO> getFbsProductStockPage(FbsProductStockPageReqVO pageReqVO) {
        return fbsProductStockMapper.selectPage(pageReqVO);
    }

}