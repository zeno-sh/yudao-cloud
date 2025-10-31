package cn.iocoder.yudao.module.dm.service.transport.dto;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/06/04 14:55
 */
@Data
public class TransportPlanItemQueryDTO {

    private Long purchaseOrderItemId;
    private Long productId;
}
