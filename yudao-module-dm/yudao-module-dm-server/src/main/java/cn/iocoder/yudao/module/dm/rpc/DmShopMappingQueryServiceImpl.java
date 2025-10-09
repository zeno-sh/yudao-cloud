package cn.iocoder.yudao.module.dm.rpc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.api.DmShopMappingQueryService;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dto.ShopMappingDTO;
import cn.iocoder.yudao.module.dm.infrastructure.AuthShopMappingService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 门店映射查询服务实现类
 * 
 * @author Jax
 * @createTime: 2025/01/16 10:00
 */
@Slf4j
@RestController
public class DmShopMappingQueryServiceImpl implements DmShopMappingQueryService {

    @Resource
    private OzonShopMappingService ozonShopMappingService;

    @Resource
    private AuthShopMappingService authShopMappingService;

    @Override
    public CommonResult<ShopMappingDTO> getShopMappingByClientId(String clientId) {
        log.info("开始查询门店映射信息, clientId: {}", clientId);
        
        if (clientId == null || clientId.trim().isEmpty()) {
            log.warn("clientId为空，返回null");
            return CommonResult.success(null);
        }
        
        try {
            OzonShopMappingDO shopMapping = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
            if (shopMapping == null) {
                log.info("未找到clientId对应的门店映射信息, clientId: {}", clientId);
                return CommonResult.success(null);
            }
            
            ShopMappingDTO result = BeanUtils.toBean(shopMapping, ShopMappingDTO.class);
            log.info("成功查询到门店映射信息, clientId: {}, shopName: {}", clientId, result.getShopName());
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("查询门店映射信息失败, clientId: {}", clientId, e);
            return CommonResult.success(null);
        }
    }

    @Override
    public CommonResult<List<ShopMappingDTO>> batchGetShopMappingByClientIds(List<String> clientIds) {
        log.info("开始批量查询门店映射信息, clientIds: {}", clientIds);
        
        if (clientIds == null || clientIds.isEmpty()) {
            log.warn("clientIds为空，返回空列表");
            return CommonResult.success(Collections.emptyList());
        }
        
        try {
            List<OzonShopMappingDO> shopMappings = ozonShopMappingService.batchShopListByClientIds(clientIds);
            if (shopMappings == null || shopMappings.isEmpty()) {
                log.info("未找到clientIds对应的门店映射信息, clientIds: {}", clientIds);
                return CommonResult.success(Collections.emptyList());
            }
            
            List<ShopMappingDTO> result = BeanUtils.toBean(shopMappings, ShopMappingDTO.class);
            log.info("成功批量查询到门店映射信息, clientIds: {}, 返回数量: {}", clientIds, result.size());
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("批量查询门店映射信息失败, clientIds: {}", clientIds, e);
            return CommonResult.success(Collections.emptyList());
        }
    }

    @Override
    public CommonResult<List<ShopMappingDTO>> getAllAvailableShopMappings() {
        log.info("开始查询所有可用的门店映射信息");
        
        try {
            List<OzonShopMappingDO> shopMappings = authShopMappingService.getAuthShopList();
            if (shopMappings == null || shopMappings.isEmpty()) {
                log.info("未找到任何门店映射信息");
                return CommonResult.success(Collections.emptyList());
            }
            
            List<ShopMappingDTO> result = BeanUtils.toBean(shopMappings, ShopMappingDTO.class);
            log.info("成功查询到所有门店映射信息, 返回数量: {}", result.size());
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("查询所有门店映射信息失败", e);
            return CommonResult.success(Collections.emptyList());
        }
    }
} 