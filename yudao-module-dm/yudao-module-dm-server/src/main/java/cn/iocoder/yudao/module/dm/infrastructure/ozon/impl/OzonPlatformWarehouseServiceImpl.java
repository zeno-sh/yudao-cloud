package cn.iocoder.yudao.module.dm.infrastructure.ozon.impl;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.WarehouseDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.WarehouseRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
import cn.iocoder.yudao.module.dm.infrastructure.service.PlatformWarehouseService;
import com.alibaba.fastjson2.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 仓库服务
 *
 * @author: Zeno
 * @createTime: 2024/08/27 22:22
 */
@Service("ozonWarehouseService")
public class OzonPlatformWarehouseServiceImpl implements PlatformWarehouseService<OzonHttpResponse<List<WarehouseDTO>>, WarehouseRequest> {

    @Resource
    private OzonHttpUtil<WarehouseRequest> ozonHttpUtil;

    @Override
    public OzonHttpResponse<List<WarehouseDTO>> queryWarehouse(WarehouseRequest warehouseQueryRequest) {
        TypeReference<OzonHttpResponse<List<WarehouseDTO>>> typeReference = new TypeReference<OzonHttpResponse<List<WarehouseDTO>>>() {
        };

        return ozonHttpUtil.post(OzonConfig.OZON_WAREHOUSE_LIST, warehouseQueryRequest, typeReference);
    }
}
