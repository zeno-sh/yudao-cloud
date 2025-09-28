package cn.iocoder.yudao.module.dm.rpc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.api.DmProductQueryService;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductInfoMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: Jax
 * @createTime: 2025/08/11 17:15
 */
@Service
public class DmProductQueryServiceImpl implements DmProductQueryService {

    @Resource
    private ProductInfoMapper productInfoMapper;

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
}
