package cn.iocoder.yudao.module.dm.infrastructure.ozon.response;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Ozon 供应订单详情响应
 *
 * @author Zeno
 */
@Data
public class SupplyOrderGetResponse {
    @JSONField(name = "orders")
    private List<Order> orders;
    @JSONField(name = "warehouses")
    private List<Warehouse> warehouses;

    @Data
    public static class Order {
        @JSONField(name = "supply_order_id")
        private String supplyOrderId;
        @JSONField(name = "supply_order_number")
        private String supplyOrderNumber;
        @JSONField(name = "creation_date")
        private String creationDate;
        @JSONField(name = "state")
        private String state;
        @JSONField(name = "dropoff_warehouse_id")
        private Long warehouseId;
        @JSONField(name = "timeslot")
        private TimeslotWrapper timeslot;
        @JSONField(name = "vehicle")
        private VehicleWrapper vehicle;
        @JSONField(name = "supplies")
        private List<Supply> supplies;
    }

    @Data
    public static class Warehouse {
        @JSONField(name = "warehouse_id")
        private Long id;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "address")
        private String address;
    }

    @Data
    public static class TimeslotWrapper {
        @JSONField(name = "value")
        private TimeslotValue value;
    }

    @Data
    public static class TimeslotValue {
        @JSONField(name = "timeslot")
        private Timeslot timeslot;
        @JSONField(name = "timezone_info")
        private TimezoneInfo timezoneInfo;
    }

    @Data
    public static class Timeslot {
        @JSONField(name = "from")
        private Date from;
        @JSONField(name = "to")
        private Date to;
    }

    @Data
    public static class TimezoneInfo {
        @JSONField(name = "offset")
        private String offset;
        @JSONField(name = "iana_name")
        private String ianaName;
    }

    @Data
    public static class VehicleWrapper {
        @JSONField(name = "value")
        private VehicleValue value;
    }

    @Data
    public static class VehicleValue {
        @JSONField(name = "vehicle_model")
        private String vehicleModel;
        @JSONField(name = "vehicle_number")
        private String vehicleNumber;
        @JSONField(name = "driver_name")
        private String driverName;
        @JSONField(name = "driver_phone")
        private String driverPhone;
    }

    @Data
    public static class Supply {
        @JSONField(name = "supply_id")
        private String supplyId;
        @JSONField(name = "bundle_id")
        private String bundleId;
        @JSONField(name = "storage_warehouse_id")
        private Long storageWarehouseId;
    }
} 