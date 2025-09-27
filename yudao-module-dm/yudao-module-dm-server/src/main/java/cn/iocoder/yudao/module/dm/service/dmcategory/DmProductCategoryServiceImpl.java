package cn.iocoder.yudao.module.dm.service.dmcategory;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.category.vo.DmProductCategoryListReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.category.vo.DmProductCategorySaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.dmcategory.DmProductCategoryDO;
import cn.iocoder.yudao.module.dm.dal.mysql.dmcategory.DmProductCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 产品分类 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class DmProductCategoryServiceImpl implements DmProductCategoryService {

    @Autowired
    private DmProductCategoryMapper productCategoryMapper;

    @Override
    public Long createProductCategory(DmProductCategorySaveReqVO createReqVO) {
        // 校验父分类编号的有效性
        validateParentProductCategory(null, createReqVO.getParentId());
        // 校验分类名称的唯一性
        validateProductCategoryNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

        // 插入
        DmProductCategoryDO productCategory = BeanUtils.toBean(createReqVO, DmProductCategoryDO.class);
        productCategoryMapper.insert(productCategory);
        // 返回
        return productCategory.getId();
    }

    @Override
    public void updateProductCategory(DmProductCategorySaveReqVO updateReqVO) {
        // 校验存在
        validateProductCategoryExists(updateReqVO.getId());
        // 校验父分类编号的有效性
        validateParentProductCategory(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验分类名称的唯一性
        validateProductCategoryNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新
        DmProductCategoryDO updateObj = BeanUtils.toBean(updateReqVO, DmProductCategoryDO.class);
        productCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductCategory(Long id) {
        // 校验存在
        validateProductCategoryExists(id);
        // 校验是否有子产品分类
        if (productCategoryMapper.selectCountByParentId(id) > 0) {
            throw exception(PRODUCT_CATEGORY_EXITS_CHILDREN);
        }
        // 删除
        productCategoryMapper.deleteById(id);
    }

    private void validateProductCategoryExists(Long id) {
        if (productCategoryMapper.selectById(id) == null) {
            throw exception(PRODUCT_CATEGORY_NOT_EXISTS);
        }
    }

    private void validateParentProductCategory(Long id, Long parentId) {
        if (parentId == null || DmProductCategoryDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父产品分类
        if (Objects.equals(id, parentId)) {
            throw exception(PRODUCT_CATEGORY_PARENT_ERROR);
        }
        // 2. 父产品分类不存在
        DmProductCategoryDO parentProductCategory = productCategoryMapper.selectById(parentId);
        if (parentProductCategory == null) {
            throw exception(PRODUCT_CATEGORY_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父产品分类，如果父产品分类是自己的子产品分类，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentProductCategory.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(PRODUCT_CATEGORY_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父产品分类
            if (parentId == null || DmProductCategoryDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentProductCategory = productCategoryMapper.selectById(parentId);
            if (parentProductCategory == null) {
                break;
            }
        }
    }

    private void validateProductCategoryNameUnique(Long id, Long parentId, String name) {
        DmProductCategoryDO productCategory = productCategoryMapper.selectByParentIdAndName(parentId, name);
        if (productCategory == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的产品分类
        if (id == null) {
            throw exception(PRODUCT_CATEGORY_NAME_DUPLICATE);
        }
        if (!Objects.equals(productCategory.getId(), id)) {
            throw exception(PRODUCT_CATEGORY_NAME_DUPLICATE);
        }
    }

    @Override
    public DmProductCategoryDO getProductCategory(Long id) {
        return productCategoryMapper.selectById(id);
    }

    @Override
    public List<DmProductCategoryDO> getProductCategoryList(DmProductCategoryListReqVO listReqVO) {
        return productCategoryMapper.selectList(listReqVO);
    }

}