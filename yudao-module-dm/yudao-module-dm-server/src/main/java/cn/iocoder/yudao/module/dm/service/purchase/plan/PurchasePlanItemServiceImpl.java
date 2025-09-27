package cn.iocoder.yudao.module.dm.service.purchase.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.PurchasePlanItemPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductInfoMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.purchase.PurchasePlanItemMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.purchase.PurchasePlanMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 采购计划详情 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class PurchasePlanItemServiceImpl implements PurchasePlanItemService {

    @Resource
    private PurchasePlanItemMapper purchasePlanItemMapper;
    @Resource
    private PurchasePlanMapper purchasePlanMapper;
    @Resource
    private ProductInfoMapper productInfoMapper;


    @Override
    public PurchasePlanItemDO getPurchasePlanItem(Long id) {
        return purchasePlanItemMapper.selectById(id);
    }

    @Override
    @DataPermission(enable = false) // 进度条场景下使用，不需要权限
    public PurchasePlanItemDO getPurchasePlanItemByPlanNumber(String planNumber) {
        return purchasePlanItemMapper.selectOne(PurchasePlanItemDO::getPlanNumber, planNumber);
    }

    @Override
    public List<PurchasePlanItemDO> getPurchasePlanItemByProductId(Long productId) {
        return purchasePlanItemMapper.selectList(PurchasePlanItemDO::getProductId, productId);
    }

    @Override
    public List<PurchasePlanItemDO> batchPurchasePlanItem(Collection<Long> productIds, Collection<String> planNumbers,
                                                          Collection<Long> userId) {
        return purchasePlanItemMapper.selectList(new LambdaQueryWrapperX<PurchasePlanItemDO>()
                .inIfPresent(PurchasePlanItemDO::getProductId, productIds)
                .inIfPresent(PurchasePlanItemDO::getPlanNumber, planNumbers)
                .inIfPresent(PurchasePlanItemDO::getCreator, userId)
        );
    }

    @Override
    public PageResult<PurchasePlanItemDO> getPurchasePlanItemPage(PurchasePlanItemPageReqVO pageReqVO) {

        if (StringUtils.isNotBlank(pageReqVO.getBatchNumber())) {
            String batchNumber = pageReqVO.getBatchNumber();
            PurchasePlanDO purchasePlanDO = purchasePlanMapper.selectOne(PurchasePlanDO::getBatchNumber, batchNumber);

            if (purchasePlanDO != null) {
                pageReqVO.setPlanId(purchasePlanDO.getId());
            }
        }

        if (StringUtils.isNotBlank(pageReqVO.getSpu())) {
            LambdaQueryWrapperX<ProductInfoDO> queryWrapperX = new LambdaQueryWrapperX<ProductInfoDO>()
                    .likeIfPresent(ProductInfoDO::getModelNumber, pageReqVO.getSpu());
            List<ProductInfoDO> productInfoDOList = productInfoMapper.selectList(queryWrapperX);

            if (CollectionUtils.isNotEmpty(productInfoDOList)) {
                List<Long> productIds = convertList(productInfoDOList, ProductInfoDO::getId);
                pageReqVO.setProductIds(productIds);
            }
        }

        return purchasePlanItemMapper.selectPage(pageReqVO);
    }

}