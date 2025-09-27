package cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo;

import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderItemDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 供应订单新增/修改 Request VO")
@Data
public class OzonSupplyOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "24795")
    private Long id;

    @Schema(description = "Ozon客户端ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6149")
    @NotEmpty(message = "Ozon客户端ID不能为空")
    private String clientId;

    @Schema(description = "Ozon供应订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7438")
    @NotNull(message = "Ozon供应订单ID不能为空")
    private Long supplyOrderId;

    @Schema(description = "订单状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "订单状态不能为空")
    private String state;

    @Schema(description = "仓库ID", example = "27899")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "李四")
    private String warehouseName;

    @Schema(description = "配送时间段开始")
    private LocalDateTime timeslotFrom;

    @Schema(description = "配送时间段结束")
    private LocalDateTime timeslotTo;

    @Schema(description = "商品总数")
    private Integer totalItems;

    @Schema(description = "总体积(升)")
    private BigDecimal totalVolume;

    @Schema(description = "供应订单商品列表")
    private List<OzonSupplyOrderItemDO> ozonSupplyOrderItems;

}