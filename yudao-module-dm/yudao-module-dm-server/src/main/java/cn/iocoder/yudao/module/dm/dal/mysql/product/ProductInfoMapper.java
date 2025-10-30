package cn.iocoder.yudao.module.dm.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.SupplierPriceOfferDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.supplier.ProductSupplierDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.*;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;

/**
 * 产品信息 Mapper
 *
 * @author zeno
 */
@Mapper
public interface ProductInfoMapper extends BaseMapperX<ProductInfoDO> {

    default PageResult<ProductInfoDO> selectPage(ProductInfoPageReqVO reqVO) {
        // 如果没有供应商相关查询条件，使用原有的简单查询
        if (StringUtils.isEmpty(reqVO.getSupplierCode())) {
            LambdaQueryWrapperX<ProductInfoDO> wrapper = new LambdaQueryWrapperX<ProductInfoDO>()
                    .likeIfPresent(ProductInfoDO::getSkuId, reqVO.getSkuId())
                    .likeIfPresent(ProductInfoDO::getSkuName, reqVO.getSkuName())
                    .eqIfPresent(ProductInfoDO::getSaleStatus, reqVO.getSaleStatus())
                    .eqIfPresent(ProductInfoDO::getCategoryId, reqVO.getCategoryId())
                    .eqIfPresent(ProductInfoDO::getBrandId, reqVO.getBrandId())
                    .eqIfPresent(ProductInfoDO::getFlagId, reqVO.getFlagId())
                    .eqIfPresent(ProductInfoDO::getDeleted, reqVO.getDeleted())
                    .eqIfPresent(ProductInfoDO::getPlatform, reqVO.getPlatform())
                    .betweenIfPresent(ProductInfoDO::getCreateTime, reqVO.getCreateTime());

            wrapper.orderByDesc(ProductInfoDO::getId);
            return selectPage(reqVO, wrapper);
        }

        // 有供应商查询条件时，使用JOIN查询
        MPJLambdaWrapper<ProductInfoDO> wrapper = new MPJLambdaWrapper<ProductInfoDO>()
                .selectAll(ProductInfoDO.class)
                .leftJoin(SupplierPriceOfferDO.class, SupplierPriceOfferDO::getProductId, ProductInfoDO::getId);
        
        // 产品信息的基本查询条件
        if (StringUtils.isNotEmpty(reqVO.getSkuId())) {
            wrapper.like(ProductInfoDO::getSkuId, reqVO.getSkuId());
        }
        if (StringUtils.isNotEmpty(reqVO.getSkuName())) {
            wrapper.like(ProductInfoDO::getSkuName, reqVO.getSkuName());
        }
        if (reqVO.getSaleStatus() != null) {
            wrapper.eq(ProductInfoDO::getSaleStatus, reqVO.getSaleStatus());
        }
        if (reqVO.getCategoryId() != null) {
            wrapper.eq(ProductInfoDO::getCategoryId, reqVO.getCategoryId());
        }
        if (reqVO.getBrandId() != null) {
            wrapper.eq(ProductInfoDO::getBrandId, reqVO.getBrandId());
        }
        if (reqVO.getFlagId() != null) {
            wrapper.eq(ProductInfoDO::getFlagId, reqVO.getFlagId());
        }
        if (reqVO.getDeleted() != null) {
            wrapper.eq(ProductInfoDO::getDeleted, reqVO.getDeleted());
        }
        if (reqVO.getPlatform() != null) {
            wrapper.eq(ProductInfoDO::getPlatform, reqVO.getPlatform());
        }
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            wrapper.between(ProductInfoDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
        }
        if (StringUtils.isNotEmpty(reqVO.getSupplierCode())) {
            wrapper.eq(SupplierPriceOfferDO::getSupplierCode, reqVO.getSupplierCode());
        }
        wrapper.groupBy(ProductInfoDO::getId).orderByDesc(ProductInfoDO::getId);
        
        return selectJoinPage(reqVO, ProductInfoDO.class, wrapper);
    }
    
    // ========== 组合产品相关方法（新增）==========
    
    /**
     * 查询指定类型和组合类型的产品列表
     *
     * @param productType 产品类型
     * @param bundleType 组合类型
     * @return 产品列表
     */
    default java.util.List<ProductInfoDO> selectListByProductTypeAndBundleType(Integer productType, Integer bundleType) {
        return selectList(new LambdaQueryWrapperX<ProductInfoDO>()
                .eq(ProductInfoDO::getProductType, productType)
                .eq(ProductInfoDO::getBundleType, bundleType));
    }

}