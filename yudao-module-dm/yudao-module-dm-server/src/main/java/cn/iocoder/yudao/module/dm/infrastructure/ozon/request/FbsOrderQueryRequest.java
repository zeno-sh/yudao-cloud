package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Data
public class FbsOrderQueryRequest extends HttpBaseRequest{

    private String dir;
    private Filter filter;
    private int limit;
    private int offset;
    private Include with;

    @Data
    public static class Filter {
        @JSONField(name = "delivery_method_id")
        private List<String> deliveryMethodId;
        @JSONField(name = "last_changed_status_date")
        private DateRange lastChangedStatusDate;
        @JSONField(name = "order_id")
        private long orderId;
        @JSONField(name = "provider_id")
        private List<String> providerId;
        private String since;
        private String status;
        private String to;
        @JSONField(name = "warehouse_id")
        private List<String> warehouseId;
    }

    @Data
    public static class DateRange {
        private Date from;
        private Date to;
    }

    @Data
    public static class Include {
        @JSONField(name = "analytics_data")
        private boolean analyticsData;
        private boolean barcodes;
        @JSONField(name = "financial_data")
        private boolean financialData;
        private boolean translit;
    }
}


