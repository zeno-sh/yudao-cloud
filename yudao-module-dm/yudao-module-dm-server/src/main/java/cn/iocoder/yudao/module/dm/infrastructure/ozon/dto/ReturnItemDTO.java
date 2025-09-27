package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Date;

/**
 * @Author zeno
 * @Date 2024/1/28
 */
@Data

public class ReturnItemDTO {

    @JSONField(name = "accepted_from_customer_moment")
    private String acceptedFromCustomerMoment;

    @JSONField(name = "commission")
    private double commission;

    @JSONField(name = "commission_percent")
    private double commissionPercent;

    @JSONField(name = "id")
    private Long id;

    @JSONField(name = "is_moving")
    private boolean isMoving;

    @JSONField(name = "is_opened")
    private boolean isOpened;

    @JSONField(name = "last_free_waiting_day")
    private String lastFreeWaitingDay;

    @JSONField(name = "moving_to_place_name")
    private String movingToPlaceName;

    @JSONField(name = "picking_amount")
    private double pickingAmount;

    @JSONField(name = "posting_number")
    private String postingNumber;

    @JSONField(name = "picking_tag")
    private String pickingTag;

    @JSONField(name = "price")
    private double price;

    @JSONField(name = "price_without_commission")
    private double priceWithoutCommission;

    @JSONField(name = "product_id")
    private int productId;

    @JSONField(name = "product_name")
    private String productName;

    @JSONField(name = "quantity")
    private int quantity;

    @JSONField(name = "return_barcode")
    private String returnBarcode;

    @JSONField(name = "return_date")
    private Date returnDate;

    @JSONField(name = "return_reason_name")
    private String returnReasonName;

    @JSONField(name = "waiting_for_seller_date_time")
    private Date waitingForSellerDateTime;

    @JSONField(name = "returned_to_seller_date_time")
    private Date returnedToSellerDateTime;

    @JSONField(name = "waiting_for_seller_days")
    private int waitingForSellerDays;

    @JSONField(name = "returns_keeping_cost")
    private double returnsKeepingCost;

    @JSONField(name = "sku")
    private int sku;

    @JSONField(name = "status")
    private String status;

}

