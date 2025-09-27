package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;

import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;

@Schema(description = "管理后台 - 采购计划新增/修改 Request VO")
@Data
public class PurchasePlanSaveReqVO {

    @Schema(description = "批次编号")
    private String batchNumber;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4271")
    private Long id;

    @Schema(description = "采购计划详情列表")
    private List<PurchasePlanItemDO> purchasePlanItems;

}