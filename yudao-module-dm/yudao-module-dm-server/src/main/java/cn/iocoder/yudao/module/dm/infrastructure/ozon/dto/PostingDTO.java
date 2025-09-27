package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Data
public class PostingDTO {
    @JSONField(name = "posting_number")
    private String postingNumber;

    @JSONField(name="order_id")
    private Long orderId;

    @JSONField(name="order_number")
    private String orderNumber;

    private String status;

    @JSONField(name="delivery_method")
    private String deliveryMethod;

    @JSONField(name="tracking_number")
    private String trackingNumber;

    @JSONField(name="tpl_integration_type")
    private String tplIntegrationType;

    @JSONField(name = "in_process_at")
    private Date inProcessAt;

    @JSONField(name = "shipment_date")
    private Date shipmentDate;

    @JSONField(name = "delivering_date")
    private Date deliveringDate;

    private String cancellation;

    private List<ProductDTO> products;

    private Object addressee; // 如果有特殊的映射，也需要添加 @JSONField

    @JSONField(name="financial_data")
    private FinancialDataDTO financialData;

    @JSONField(name="is_express")
    private boolean isExpress;

    @JSONField(name="parent_posting_number")
    private String parentPostingNumber;

    @JSONField(name="multi_box_qty")
    private Integer multiBoxQty;

    @JSONField(name="is_multibox")
    private boolean isMultibox;

    private String substatus;

    @JSONField(name="prr_option")
    private String prrOption;

    private BarcodesDTO barcodes;
}
