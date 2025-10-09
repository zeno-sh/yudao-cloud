package cn.iocoder.yudao.module.dm.rpc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.api.DmProductQueryService;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductInfoMapper;
import cn.iocoder.yudao.module.dm.dto.ProductSimpleInfoDTO;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
}
