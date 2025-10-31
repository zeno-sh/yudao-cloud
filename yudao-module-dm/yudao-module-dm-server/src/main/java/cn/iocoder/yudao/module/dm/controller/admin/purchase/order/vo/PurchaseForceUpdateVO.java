package cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo;

import lombok.Data;

/**
 * 强制更新采购单-主要用于修改采购数量
 * @author: Zeno
 * @createTime: 2025/01/17 14:43
 */
@Data
public class PurchaseForceUpdateVO {

    private Long purchaseOrderId;

    private Long purchaseOrderItemId;
}
