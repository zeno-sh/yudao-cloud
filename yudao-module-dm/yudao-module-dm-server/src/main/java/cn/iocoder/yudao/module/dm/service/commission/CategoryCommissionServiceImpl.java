package cn.iocoder.yudao.module.dm.service.commission;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import cn.iocoder.yudao.module.dm.controller.admin.commission.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.commission.CategoryCommissionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.commission.CategoryCommissionMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 类目佣金 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class CategoryCommissionServiceImpl implements CategoryCommissionService {

    @Resource
    private CategoryCommissionMapper categoryCommissionMapper;

    @Override
    public Long createCategoryCommission(CategoryCommissionSaveReqVO createReqVO) {
        // 校验平台的有效性
        validateParentCategoryCommission(null, createReqVO.getParentId());
        // 校验类目名称的唯一性
        validateCategoryCommissionCategoryNameUnique(null, createReqVO.getParentId(), createReqVO.getCategoryName());

        // 插入
        CategoryCommissionDO categoryCommission = BeanUtils.toBean(createReqVO, CategoryCommissionDO.class);
        categoryCommissionMapper.insert(categoryCommission);
        // 返回
        return categoryCommission.getId();
    }

    @Override
    public void updateCategoryCommission(CategoryCommissionSaveReqVO updateReqVO) {
        // 校验存在
        validateCategoryCommissionExists(updateReqVO.getId());
        // 校验平台的有效性
        validateParentCategoryCommission(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验类目名称的唯一性
        validateCategoryCommissionCategoryNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getCategoryName());

        // 更新
        CategoryCommissionDO updateObj = BeanUtils.toBean(updateReqVO, CategoryCommissionDO.class);
        categoryCommissionMapper.updateById(updateObj);
    }

    @Override
    public void deleteCategoryCommission(Long id) {
        // 校验存在
        validateCategoryCommissionExists(id);
        // 校验是否有子类目佣金
        if (categoryCommissionMapper.selectCountByParentId(id) > 0) {
            throw exception(CATEGORY_COMMISSION_EXITS_CHILDREN);
        }
        // 删除
        categoryCommissionMapper.deleteById(id);
    }

    private void validateCategoryCommissionExists(Long id) {
        if (categoryCommissionMapper.selectById(id) == null) {
            throw exception(CATEGORY_COMMISSION_NOT_EXISTS);
        }
    }

    private void validateParentCategoryCommission(Long id, Long parentId) {
        if (parentId == null || CategoryCommissionDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父类目佣金
        if (Objects.equals(id, parentId)) {
            throw exception(CATEGORY_COMMISSION_PARENT_ERROR);
        }
        // 2. 父类目佣金不存在
        CategoryCommissionDO parentCategoryCommission = categoryCommissionMapper.selectById(parentId);
        if (parentCategoryCommission == null) {
            throw exception(CATEGORY_COMMISSION_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父类目佣金，如果父类目佣金是自己的子类目佣金，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentCategoryCommission.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(CATEGORY_COMMISSION_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父类目佣金
            if (parentId == null || CategoryCommissionDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentCategoryCommission = categoryCommissionMapper.selectById(parentId);
            if (parentCategoryCommission == null) {
                break;
            }
        }
    }

    private void validateCategoryCommissionCategoryNameUnique(Long id, Long parentId, String categoryName) {
        CategoryCommissionDO categoryCommission = categoryCommissionMapper.selectByParentIdAndCategoryName(parentId, categoryName);
        if (categoryCommission == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的类目佣金
        if (id == null) {
            throw exception(CATEGORY_COMMISSION_CATEGORY_NAME_DUPLICATE);
        }
        if (!Objects.equals(categoryCommission.getId(), id)) {
            throw exception(CATEGORY_COMMISSION_CATEGORY_NAME_DUPLICATE);
        }
    }

    @Override
    public CategoryCommissionDO getCategoryCommission(Long id) {
        return categoryCommissionMapper.selectById(id);
    }

    @Override
    public List<CategoryCommissionDO> getCategoryCommissionList(CategoryCommissionListReqVO listReqVO) {
        return categoryCommissionMapper.selectList(listReqVO);
    }

    @Override
    public List<CategoryCommissionTreeRespVO> getCategoryCommissionTree() {
        List<CategoryCommissionDO> flatList = categoryCommissionMapper.selectAllActiveCategories();
        Map<Long, CategoryCommissionTreeRespVO> map = new HashMap<>();

        // 将 DO 转换为 VO，并创建 ID 到 VO 对象的映射
        for (CategoryCommissionDO dobj : flatList) {
            CategoryCommissionTreeRespVO vo = new CategoryCommissionTreeRespVO();
            vo.setId(dobj.getId());
            vo.setParentId(dobj.getParentId());
            vo.setCategoryName(dobj.getCategoryName());
            vo.setRate(dobj.getRate());
            map.put(vo.getId(), vo);
        }

        List<CategoryCommissionTreeRespVO> result = new ArrayList<>();

        // 构建树形结构
        for (CategoryCommissionTreeRespVO vo : map.values()) {
            if (vo.getParentId() != null && !vo.getParentId().equals(CategoryCommissionDO.PARENT_ID_ROOT)) {
                if (map.get(vo.getParentId()) != null) {
                    if (map.get(vo.getParentId()).getChildrenList() == null) {
                        map.get(vo.getParentId()).setChildrenList(new ArrayList<>());
                    }
                    map.get(vo.getParentId()).getChildrenList().add(vo);
                }
            } else {
                // 根节点
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    public List<CategoryCommissionDO> batchQueryByIds(List<Long> ids) {
        return categoryCommissionMapper.selectList("id", ids);
    }
}