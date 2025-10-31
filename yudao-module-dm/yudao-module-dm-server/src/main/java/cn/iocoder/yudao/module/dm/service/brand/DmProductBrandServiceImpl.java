package cn.iocoder.yudao.module.dm.service.brand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.dm.controller.admin.brand.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.brand.DmProductBrandDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.brand.DmProductBrandMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 品牌信息 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class DmProductBrandServiceImpl implements DmProductBrandService {

    @Autowired
    private DmProductBrandMapper productBrandMapper;

    @Override
    public Long createProductBrand(DmProductBrandSaveReqVO createReqVO) {
        // 插入
        DmProductBrandDO productBrand = BeanUtils.toBean(createReqVO, DmProductBrandDO.class);
        productBrandMapper.insert(productBrand);
        // 返回
        return productBrand.getId();
    }

    @Override
    public void updateProductBrand(DmProductBrandSaveReqVO updateReqVO) {
        // 校验存在
        validateProductBrandExists(updateReqVO.getId());
        // 更新
        DmProductBrandDO updateObj = BeanUtils.toBean(updateReqVO, DmProductBrandDO.class);
        productBrandMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductBrand(Long id) {
        // 校验存在
        validateProductBrandExists(id);
        // 删除
        productBrandMapper.deleteById(id);
    }

    private void validateProductBrandExists(Long id) {
        if (productBrandMapper.selectById(id) == null) {
            throw exception(PRODUCT_BRAND_NOT_EXISTS);
        }
    }

    @Override
    public DmProductBrandDO getProductBrand(Long id) {
        return productBrandMapper.selectById(id);
    }

    @Override
    public PageResult<DmProductBrandDO> getProductBrandPage(DmProductBrandPageReqVO pageReqVO) {
        return productBrandMapper.selectPage(pageReqVO);
    }

}