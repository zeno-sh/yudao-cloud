package cn.iocoder.yudao.module.dm.infrastructure.wb.constant;

/**
 * @author: Zeno
 * @createTime: 2024/07/10 23:27
 */
public class WbConfig {

    public static final String WB_DOMAIN = "https://marketplace-api.wildberries.ru";

    public static final String WB_CONTENT_DOMAIN = "https://content-api.wildberries.ru";

    public static final String WB_ORDER_LIST_API = WB_DOMAIN + "/api/v3/orders";

    public static final String WB_ORDER_STATUS_API = WB_DOMAIN + "/api/v3/orders/status";

    public static final String WB_NEW_ORDER_API = WB_DOMAIN + "/api/v3/orders/new";

    public static final String WB_PRODUCT_LIST_API = WB_CONTENT_DOMAIN + "/content/v2/get/cards/list?local=zh";

    public static final String WB_All_PRODUCT_LIST_API ="https://statistics-api.wildberries.ru/api/v1/supplier/orders";

    public static final String WB_WAREHOUSE_LIST_API = WB_DOMAIN + "/api/v3/warehouses";
}
