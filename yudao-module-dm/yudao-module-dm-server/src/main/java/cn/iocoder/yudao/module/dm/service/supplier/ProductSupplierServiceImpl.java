package cn.iocoder.yudao.module.dm.service.supplier;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.dm.dal.redis.dao.DmNoRedisDAO;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.dm.controller.admin.supplier.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.supplier.ProductSupplierDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.supplier.ProductSupplierMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 供应商信息 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class ProductSupplierServiceImpl implements ProductSupplierService {

    @Resource
    private ProductSupplierMapper productSupplierMapper;
    @Resource
    private DmNoRedisDAO dmNoRedisDAO;

    @Override
    public Long createProductSupplier(ProductSupplierSaveReqVO createReqVO) {
        // 插入
        ProductSupplierDO productSupplier = BeanUtils.toBean(createReqVO, ProductSupplierDO.class);
        String supplierCode = dmNoRedisDAO.generate(DmNoRedisDAO.SUPPLIER_NO_PREFIX);
        productSupplier.setSupplierCode(supplierCode);

        productSupplierMapper.insert(productSupplier);
        // 返回
        return productSupplier.getId();
    }

    @Override
    public void updateProductSupplier(ProductSupplierSaveReqVO updateReqVO) {
        // 校验存在
        validateProductSupplierExists(updateReqVO.getId());
        // 更新
        ProductSupplierDO updateObj = BeanUtils.toBean(updateReqVO, ProductSupplierDO.class);
        productSupplierMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductSupplier(Long id) {
        // 校验存在
        validateProductSupplierExists(id);
        // 删除
        productSupplierMapper.deleteById(id);
    }

    private void validateProductSupplierExists(Long id) {
        if (productSupplierMapper.selectById(id) == null) {
            throw exception(PRODUCT_SUPPLIER_NOT_EXISTS);
        }
    }

    @Override
    public ProductSupplierDO getProductSupplier(Long id) {
        return productSupplierMapper.selectById(id);
    }

    @Override
    public PageResult<ProductSupplierDO> getProductSupplierPage(ProductSupplierPageReqVO pageReqVO) {
        return productSupplierMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductSupplierDO> batchQueryProductSupplierByIds(Collection<Long> ids) {
        return productSupplierMapper.selectList(ProductSupplierDO::getId, ids);
    }

    @Override
    public Map<Long, ProductSupplierDO> getSupplierMap(Collection<Long> ids) {
        List<ProductSupplierDO> productSupplierDOS = batchQueryProductSupplierByIds(ids);
        return CollectionUtils.convertMap(productSupplierDOS, ProductSupplierDO::getId);
    }
}