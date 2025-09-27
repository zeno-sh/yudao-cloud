package cn.iocoder.yudao.module.dm.infrastructure.wb;

import cn.iocoder.yudao.module.dm.infrastructure.service.PlatformWarehouseService;
import cn.iocoder.yudao.module.dm.infrastructure.wb.constant.WbConfig;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Warehouse;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbWarehouseQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.wb.utils.WbHttpUtils;
import com.alibaba.fastjson2.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/28 17:33
 */
@Service("wbWarehouseService")
public class WbPlatformWarehouseServiceImpl implements PlatformWarehouseService<List<Warehouse>, WbWarehouseQueryRequest> {

    @Resource
    private WbHttpUtils wbHttpUtils;

    @Override
    public List<Warehouse> queryWarehouse(WbWarehouseQueryRequest warehouseQueryRequest) {

        TypeReference<List<Warehouse>> typeReference = new TypeReference<List<Warehouse>>() {
        };
        return wbHttpUtils.get2(warehouseQueryRequest.getToken(), WbConfig.WB_WAREHOUSE_LIST_API, null, typeReference);
    }
}
