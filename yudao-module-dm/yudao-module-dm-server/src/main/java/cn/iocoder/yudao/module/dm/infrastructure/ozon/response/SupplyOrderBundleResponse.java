package cn.iocoder.yudao.module.dm.infrastructure.ozon.response;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ozon 供应订单商品响应
 *
 * @author Zeno
 */
@Data
public class SupplyOrderBundleResponse {
    @JSONField(name = "items")
    private List<Item> items;
    @JSONField(name = "total_count")
    private Integer totalCount;
    @JSONField(name = "has_next")
    private Boolean hasNext;
    @JSONField(name = "last_id")
    private String lastId;

    @Data
    public static class Item {
        @JSONField(name = "sku")
        private Long sku;
        @JSONField(name = "icon_path")
        private String iconPath;
        @JSONField(name = "offer_id")
        private String offerId;
        @JSONField(name = "product_id")
        private Long productId;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "quantity")
        private Integer quantity;
        @JSONField(name = "quant")
        private Integer quant;
        @JSONField(name = "barcode")
        private String barcode;
        @JSONField(name = "volume_in_litres")
        private BigDecimal volume;
        @JSONField(name = "total_volume_in_litres")
        private BigDecimal totalVolume;
        @JSONField(name = "contractor_item_code")
        private String contractorItemCode;
        @JSONField(name = "sfbo_attribute")
        private String sfboAttribute;
        @JSONField(name = "shipment_type")
        private String shipmentType;
    }
} 