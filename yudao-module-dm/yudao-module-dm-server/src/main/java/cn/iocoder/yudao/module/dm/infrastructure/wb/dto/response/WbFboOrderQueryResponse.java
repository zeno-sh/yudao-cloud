
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response;

import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Order;
import lombok.Data;

import java.util.List;

@Data
public class WbFboOrderQueryResponse extends WbHttpBaseResponse {

    private List<Order> orders;
}
