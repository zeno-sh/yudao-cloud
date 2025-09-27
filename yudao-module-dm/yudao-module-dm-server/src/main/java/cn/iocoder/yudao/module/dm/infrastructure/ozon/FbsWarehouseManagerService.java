package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.WarehouseDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.WarehouseRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.service.PlatformWarehouseService;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Warehouse;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbWarehouseQueryRequest;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/27 22:46
 */
@Service
public class FbsWarehouseManagerService {

    @Resource
    private PlatformWarehouseService<OzonHttpResponse<List<WarehouseDTO>>, WarehouseRequest> ozonPlatformWarehouseService;
    @Resource
    private PlatformWarehouseService<List<Warehouse>, WbWarehouseQueryRequest> wbPlatformWarehouseService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;

    public List<FbsWarehouseMappingDO> queryPlatformWarehouseList(List<String> clientIds) {
        List<FbsWarehouseMappingDO> fbsWarehouseMappingDOList = new ArrayList<>();
        List<OzonShopMappingDO> ozonShopMappingDOList = ozonShopMappingService.batchShopListByClientIds(clientIds);
        for (OzonShopMappingDO ozonShopMappingDO : ozonShopMappingDOList) {
            fbsWarehouseMappingDOList.addAll(convert(ozonShopMappingDO));
        }

        return fbsWarehouseMappingDOList;
    }

    private List<FbsWarehouseMappingDO> convert(OzonShopMappingDO ozonShopMappingDO) {
        List<FbsWarehouseMappingDO> fbsWarehouseMappingDOList = new ArrayList<>();

        if (ozonShopMappingDO.getPlatform().equals(DmPlatformEnum.OZON.getPlatformId()) ||
                ozonShopMappingDO.getPlatform().equals(DmPlatformEnum.OZON_GLOBAL.getPlatformId())) {
            WarehouseRequest request = new WarehouseRequest();
            request.setClientId(ozonShopMappingDO.getClientId());
            request.setApiKey(ozonShopMappingDO.getApiKey());

            OzonHttpResponse<List<WarehouseDTO>> response = ozonPlatformWarehouseService.queryWarehouse(request);
            if (null != response && CollectionUtils.isNotEmpty(response.getResult())) {
                List<WarehouseDTO> warehouseDTOList = response.getResult();
                for (WarehouseDTO warehouseDTO : warehouseDTOList) {
                    FbsWarehouseMappingDO fbsWarehouseMappingDO = new FbsWarehouseMappingDO();
                    fbsWarehouseMappingDO.setPlatformWarehouseId(String.valueOf(warehouseDTO.getWarehouseId()));
                    fbsWarehouseMappingDO.setPlatformWarehouseName(warehouseDTO.getName());
                    fbsWarehouseMappingDO.setClientId(ozonShopMappingDO.getClientId());
                    fbsWarehouseMappingDO.setCargoType(warehouseDTO.getIsKgt() ? 20 : 10);
                    fbsWarehouseMappingDOList.add(fbsWarehouseMappingDO);
                }
            }
        }

        if (ozonShopMappingDO.getPlatform().equals(DmPlatformEnum.WB.getPlatformId()) ||
                ozonShopMappingDO.getPlatform().equals(DmPlatformEnum.WB_GLOBAL.getPlatformId())) {

            WbWarehouseQueryRequest wbWarehouseQueryRequest = new WbWarehouseQueryRequest();
            wbWarehouseQueryRequest.setClientId(ozonShopMappingDO.getClientId());
            wbWarehouseQueryRequest.setToken(ozonShopMappingDO.getApiKey());

            List<Warehouse> warehouseList = wbPlatformWarehouseService.queryWarehouse(wbWarehouseQueryRequest);
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                for (Warehouse warehouse : warehouseList) {
                    FbsWarehouseMappingDO fbsWarehouseMappingDO = new FbsWarehouseMappingDO();
                    fbsWarehouseMappingDO.setPlatformWarehouseId(String.valueOf(warehouse.getId()));
                    fbsWarehouseMappingDO.setPlatformWarehouseName(warehouse.getName());
                    fbsWarehouseMappingDO.setClientId(ozonShopMappingDO.getClientId());
                    fbsWarehouseMappingDO.setCargoType(warehouse.getCargoType() == 1 ? 10 : 20);
                    fbsWarehouseMappingDOList.add(fbsWarehouseMappingDO);
                }
            }
        }

        return fbsWarehouseMappingDOList;
    }
}
