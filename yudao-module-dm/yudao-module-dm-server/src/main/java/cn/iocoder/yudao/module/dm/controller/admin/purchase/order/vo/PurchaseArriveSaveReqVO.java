package cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/05/14 11:25
 */
@Schema(description = "管理后台 - 到货 Request VO")
@Data
public class PurchaseArriveSaveReqVO {

    private Long id;

    private String batchNumber;

    private List<PurchaseOrderItemVO> purchaseOrderItems;
}
