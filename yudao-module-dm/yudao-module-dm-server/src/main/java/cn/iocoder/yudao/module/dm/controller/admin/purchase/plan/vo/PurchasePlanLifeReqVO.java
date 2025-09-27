package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;


/**
 * @author: Zeno
 * @createTime: 2024/06/13 10:43
 */
@Schema(description = "管理后台")
@Data
@ToString(callSuper = true)
public class PurchasePlanLifeReqVO {

    @Schema(description = "采购单号")
    private String orderNo;

    @Schema(description = "产品spu")
    private String spu;

    @Schema(description = "产品sku")
    private String sku;

    @Schema(description = "计划单号")
    private String planNumber;

    @Schema(description = "计划单号")
    private String planBatchNumber;

    @Schema(description = "指定用户")
    private List<Long> appointUserIds;

    @Schema(description = "状态")
    /**
     * @see cn.iocoder.yudao.module.dm.enums.PurchaseLifeStatusEnum
     */
    private String status;

}
