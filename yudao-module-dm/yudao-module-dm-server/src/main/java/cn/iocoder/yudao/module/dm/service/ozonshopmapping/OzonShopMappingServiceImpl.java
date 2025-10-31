package cn.iocoder.yudao.module.dm.service.ozonshopmapping;

import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ClientSimpleInfoDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.dm.controller.admin.shop.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.ozonshopmapping.OzonShopMappingMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.convertMap;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * ozon店铺 Service 实现类
 *
 * @author zeno
 */
@Service
@Validated
public class OzonShopMappingServiceImpl implements OzonShopMappingService {

    @Resource
    private OzonShopMappingMapper ozonShopMappingMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Integer createOzonShopMapping(OzonShopMappingSaveReqVO createReqVO) {
        // 插入
        OzonShopMappingDO ozonShopMapping = BeanUtils.toBean(createReqVO, OzonShopMappingDO.class);
        ozonShopMappingMapper.insert(ozonShopMapping);
        // 返回
        return ozonShopMapping.getId();
    }

    @Override
    public void updateOzonShopMapping(OzonShopMappingSaveReqVO updateReqVO) {
        // 校验存在
        validateOzonShopMappingExists(updateReqVO.getId());
        // 更新
        OzonShopMappingDO updateObj = BeanUtils.toBean(updateReqVO, OzonShopMappingDO.class);
        ozonShopMappingMapper.updateById(updateObj);
    }

    @Override
    public void deleteOzonShopMapping(Integer id) {
        // 校验存在
        validateOzonShopMappingExists(id);
        // 删除
        ozonShopMappingMapper.deleteById(id);
    }

    private void validateOzonShopMappingExists(Integer id) {
        if (ozonShopMappingMapper.selectById(id) == null) {
            throw exception(OZON_SHOP_MAPPING_NOT_EXISTS);
        }
    }

    @Override
    public OzonShopMappingDO getOzonShopMapping(Integer id) {
        return ozonShopMappingMapper.selectById(id);
    }

    @Override
    public List<OzonShopMappingSimpleRespVO> getOzonShopMappingByAuth() {
        // 获取当前登录用户ID
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        AdminUserRespDTO user = adminUserApi.getUser(loginUserId).getCheckedData();
        Set<Long> shopIds = user.getShopIds();
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyList();
        }

        List<OzonShopMappingDO> shopList = ozonShopMappingMapper.selectList(OzonShopMappingDO::getId, shopIds);

        List<OzonShopMappingSimpleRespVO> platformGroups = shopList.stream()
                .collect(Collectors.groupingBy(OzonShopMappingDO::getPlatform))
                .entrySet()
                .stream()
                .map(entry -> {
                    OzonShopMappingSimpleRespVO platformGroup = new OzonShopMappingSimpleRespVO();
                    platformGroup.setPlatform(entry.getKey());
                    platformGroup.setPlatformName(getPlatformNameById(entry.getKey()));
                    platformGroup.setChildrenList(entry.getValue().stream()
                            .map(this::convertToVO)
                            .collect(Collectors.toList()));
                    return platformGroup;
                })
                .collect(Collectors.toList());


        return platformGroups;
    }

    private OzonShopMappingSimpleRespVO convertToVO(OzonShopMappingDO shopDO) {
        OzonShopMappingSimpleRespVO shopVO = new OzonShopMappingSimpleRespVO();
        shopVO.setId(shopDO.getId());
        shopVO.setPlatform(shopDO.getPlatform());
        // 假设 platformName 是通过 platform 获取的，这里需要自己实现获取平台名称的逻辑
        shopVO.setPlatformName(getPlatformNameById(shopDO.getPlatform()));
        shopVO.setShopName(shopDO.getShopName());
        shopVO.setClientId(shopDO.getClientId());
        shopVO.setChildrenList(null); // 这里初始化为空，树形结构由平台分组实现
        return shopVO;
    }

    private String getPlatformNameById(Integer platformId) {
        // 实现根据平台 ID 获取平台名称的逻辑
        return DictFrameworkUtils.parseDictDataLabel("dm_platform", platformId);
    }

    @Override
    public OzonShopMappingDO getOzonShopMappingByClientId(String clientId) {
        return ozonShopMappingMapper.selectOne(OzonShopMappingDO::getClientId, clientId);
    }

    @Override
    public PageResult<OzonShopMappingDO> getOzonShopMappingPage(OzonShopMappingPageReqVO pageReqVO) {
        return ozonShopMappingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OzonShopMappingDO> getOzonShopList() {
        return ozonShopMappingMapper.selectList();
    }

    @Override
    public List<OzonShopMappingDO> getShopListByIds(Collection<Long> ids) {
        return ozonShopMappingMapper.selectList(OzonShopMappingDO::getId, ids);
    }

    @Override
    public List<OzonShopMappingDO> batchShopListByClientIds(Collection<String> clientIds) {
        return ozonShopMappingMapper.selectList(OzonShopMappingDO::getClientId, clientIds);
    }

    @Override
    public Map<String, ClientSimpleInfoDTO> batchSimpleInfoByClientIds(List<String> clientIds) {
        List<OzonShopMappingDO> ozonShopMappingDOList = ozonShopMappingMapper.selectList(OzonShopMappingDO::getClientId, clientIds);
        if (CollectionUtils.isEmpty(ozonShopMappingDOList)) {
            return Collections.emptyMap();
        }
        return ozonShopMappingDOList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        OzonShopMappingDO::getClientId,
                        ozonShopMappingDO -> {
                            ClientSimpleInfoDTO clientSimpleInfoDTO = new ClientSimpleInfoDTO();
                            clientSimpleInfoDTO.setClientId(ozonShopMappingDO.getClientId());
                            clientSimpleInfoDTO.setShopName(ozonShopMappingDO.getShopName());
                            clientSimpleInfoDTO.setPlatform(ozonShopMappingDO.getPlatform());
                            clientSimpleInfoDTO.setPlatformName(DictFrameworkUtils.parseDictDataValue("dm_platform", String.valueOf(ozonShopMappingDO.getPlatform())));

                            return clientSimpleInfoDTO;
                        },
                        (existing, replacement) -> existing // 处理键冲突的策略
                ));
    }
}