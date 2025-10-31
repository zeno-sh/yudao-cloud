package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response;

import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Warehouse;
import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/28 17:37
 */
@Data
public class WbWarehouseQueryResponse extends WbHttpBaseResponse{
    private List<Warehouse> warehouseList;
}
