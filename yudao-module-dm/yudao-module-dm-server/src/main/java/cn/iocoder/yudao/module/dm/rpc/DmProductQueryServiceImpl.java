package cn.iocoder.yudao.module.dm.rpc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.api.DmProductQueryService;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductBundleRelationDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductBundleRelationMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductInfoMapper;
import cn.iocoder.yudao.module.dm.dto.ProductBundleRelationDTO;
import cn.iocoder.yudao.module.dm.dto.ProductSimpleInfoDTO;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Jax
 * @createTime: 2025/08/11 17:15
 */
@RestController
public class DmProductQueryServiceImpl implements DmProductQueryService {

    @Resource
    private ProductInfoMapper productInfoMapper;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductBundleRelationMapper productBundleRelationMapper;

    @Override
    public CommonResult<Set<Long>> getProductIdsByUserPermission() {

        // 框架会根据 ProductDataPermissionRule 规则获取用户权限内的产品ID
        LambdaQueryWrapperX<ProductInfoDO> wrapperX = new LambdaQueryWrapperX<>();

        List<ProductInfoDO> productInfoDOS = productInfoMapper.selectList(wrapperX);
        if (CollectionUtils.isNotEmpty(productInfoDOS)) {
            return CommonResult.success(productInfoDOS.stream().map(ProductInfoDO::getId).collect(Collectors.toSet()));
        }

        return CommonResult.success(Collections.emptySet());
    }

    @Override
    public CommonResult<Map<Long, ProductSimpleInfoDTO>> batchQueryProductSimpleInfo(List<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return CommonResult.success(Collections.emptyMap());
        }

        // 调用 ProductInfoService 的批量查询方法
        Map<Long, ProductSimpleInfoVO> voMap = productInfoService.batchQueryProductSimpleInfo(productIds);

        // 将 VO 转换为 DTO
        Map<Long, ProductSimpleInfoDTO> dtoMap = voMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> BeanUtils.toBean(entry.getValue(), ProductSimpleInfoDTO.class)
                ));

        return CommonResult.success(dtoMap);
    }

    @Override
    public CommonResult<List<ProductBundleRelationDTO>> getBundleRelations(Long bundleProductId) {
        if (bundleProductId == null) {
            return CommonResult.success(Collections.emptyList());
        }

        // 查询组合产品关系
        List<ProductBundleRelationDO> relations = productBundleRelationMapper
                .selectListByBundleProductId(bundleProductId);

        if (CollectionUtils.isEmpty(relations)) {
            return CommonResult.success(Collections.emptyList());
        }

        // 转换为DTO并填充子产品信息
        List<ProductBundleRelationDTO> dtoList = new ArrayList<>();
        for (ProductBundleRelationDO relation : relations) {
            ProductInfoDO subProduct = productInfoMapper.selectById(relation.getSubProductId());
            if (subProduct != null) {
                ProductBundleRelationDTO dto = BeanUtils.toBean(relation, ProductBundleRelationDTO.class);
                dto.setSubSkuId(subProduct.getSkuId());
                dto.setSubSkuName(subProduct.getSkuName());
                dtoList.add(dto);
            }
        }

        return CommonResult.success(dtoList);
    }

    @Override
    public CommonResult<Map<Long, Integer>> getProductTypes(List<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return CommonResult.success(Collections.emptyMap());
        }

        // 批量查询产品信息
        List<ProductInfoDO> products = productInfoMapper.selectBatchIds(productIds);

        if (CollectionUtils.isEmpty(products)) {
            return CommonResult.success(Collections.emptyMap());
        }

        // 构建产品ID到产品类型的映射
        Map<Long, Integer> typeMap = products.stream()
                .collect(Collectors.toMap(
                        ProductInfoDO::getId,
                        product -> product.getProductType() != null ? product.getProductType() : 0
                ));

        return CommonResult.success(typeMap);
    }
}
