package cn.iocoder.yudao.module.dm.service.productcosts;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.dm.controller.admin.productcosts.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.productcosts.ProductCostsMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 产品成本结构 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class ProductCostsServiceImpl implements ProductCostsService {

    @Resource
    private ProductCostsMapper productCostsMapper;

    @Override
    public Long createProductCosts(ProductCostsSaveReqVO createReqVO) {
        // 插入
        ProductCostsDO productCosts = BeanUtils.toBean(createReqVO, ProductCostsDO.class);
        productCostsMapper.insert(productCosts);
        // 返回
        return productCosts.getId();
    }

    @Override
    public void updateProductCosts(ProductCostsSaveReqVO updateReqVO) {
        // 校验存在
        validateProductCostsExists(updateReqVO.getId());
        // 更新
        ProductCostsDO updateObj = BeanUtils.toBean(updateReqVO, ProductCostsDO.class);
        productCostsMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductCosts(Long id) {
        // 校验存在
        validateProductCostsExists(id);
        // 删除
        productCostsMapper.deleteById(id);
    }

    @Override
    public void deleteProductCostsByProductId(Long productId) {
        productCostsMapper.delete(ProductCostsDO::getProductId, productId);
    }

    private void validateProductCostsExists(Long id) {
        if (productCostsMapper.selectById(id) == null) {
            throw exception(PRODUCT_COSTS_NOT_EXISTS);
        }
    }

    @Override
    public ProductCostsDO getProductCosts(Long id) {
        return productCostsMapper.selectById(id);
    }

    @Override
    public ProductCostsDO getProductCostsByProductIdAndPlatform(Long productId, Integer platform) {
        if (productId == null || platform == null) {
            return null;
        }
        return productCostsMapper.selectOne(ProductCostsDO::getProductId, productId, 
                                            ProductCostsDO::getPlatform, platform);
    }

    @Override
    public PageResult<ProductCostsDO> getProductCostsPage(ProductCostsPageReqVO pageReqVO) {
        return productCostsMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductCostsDO> getProductCostsListByProductId(Long productId) {
        return productCostsMapper.selectList(ProductCostsDO::getProductId, productId);
    }

    @Override
    public List<ProductCostsDO> batchProductCostsListByProductIds(Collection<Long> productIds) {
        return productCostsMapper.selectList(ProductCostsDO::getProductId, productIds);
    }
}