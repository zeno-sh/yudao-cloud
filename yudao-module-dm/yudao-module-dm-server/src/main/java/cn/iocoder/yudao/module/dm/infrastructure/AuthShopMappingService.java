package cn.iocoder.yudao.module.dm.infrastructure;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.OZON_SHOP_MAPPING_NO_AUTH;

/**
 * @author: Zeno
 * @createTime: 2024/07/08 21:55
 */
@Service
public class AuthShopMappingService {

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private OzonShopMappingService ozonShopMappingService;

    public List<OzonShopMappingDO> getAuthShopList() {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (loginUserId == null) {
            throw exception(OZON_SHOP_MAPPING_NO_AUTH);
        }

        AdminUserRespDTO user = adminUserApi.getUser(loginUserId).getCheckedData();
        Set<Long> shopIds = user.getShopIds();
        if (CollectionUtils.isEmpty(shopIds)) {
            throw exception(OZON_SHOP_MAPPING_NO_AUTH);
        }

        List<OzonShopMappingDO> shopList = ozonShopMappingService.getShopListByIds(shopIds);
        if (CollectionUtils.isEmpty(shopList)) {
            throw exception(OZON_SHOP_MAPPING_NO_AUTH);
        }

        return shopList;
    }
}
