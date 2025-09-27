package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author zeno
 * @Date 2024/1/31
 */
@Data
public class FinanceTransactionDTO {
    @JSONField(name = "operation_id")
    private Long operationId;

    @JSONField(name = "operation_type")
    private String operationType;

    @JSONField(name = "operation_date")
    private String operationDate;

    @JSONField(name = "operation_type_name")
    private String operationTypeName;

    @JSONField(name = "delivery_charge")
    private BigDecimal deliveryCharge;

    @JSONField(name = "return_delivery_charge")
    private BigDecimal returnDeliveryCharge;

    @JSONField(name = "accruals_for_sale")
    private BigDecimal accrualsForSale;

    @JSONField(name = "sale_commission")
    private BigDecimal saleCommission;

    private BigDecimal amount;
    private String type;
    private PostingsDTO posting;
    private List<ItemDTO> items;
    private List<ServiceDTO> services;
}
