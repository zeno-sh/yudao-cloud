package cn.iocoder.yudao.module.dm.infrastructure.ozon.constant;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
public class OzonConfig {

    public static final String OZON_DOMAIN = "https://api-seller.ozon.ru";
    /**
     * 货件列表
     */
    public static final String OZON_POSTING_API = OZON_DOMAIN + "/v3/posting/fbs/list";

    /**
     * fbo货件列表
     */
    public static final String OZON_FBO_POSTING_API = OZON_DOMAIN + "/v2/posting/fbo/list";

    /**
     * 财务清单
     */
    public static final String OZON_FINANCE_API = OZON_DOMAIN + "/v3/finance/transaction/totals";

    /**
     * 财务清单
     */
    public static final String OZON_FINANCE_TRANSACTION_API = OZON_DOMAIN + "/v3/finance/transaction/list";

    /**
     * 退货列表
     */
    public static final String OZON_ORDER_RETURN_API = OZON_DOMAIN + "/v3/returns/company/fbs";

    /**
     * 根据ID获取货件详情
     */
    public static final String OZON_ORDER_POSTING_INFO = OZON_DOMAIN + "/v3/posting/fbs/get";


    /**
     * 根据ID获取货件详情
     */
    public static final String OZON_ORDER_FBO_POSTING_INFO = OZON_DOMAIN + "/v2/posting/fbo/get";

    /**
     * 商品列表
     */
    public static final String OZON_PRODUCT_LIST_API = OZON_DOMAIN + "/v3/product/list";

    /**
     * 产品详情
     */
    public static final String OZON_PRODUCT_INFO_API = OZON_DOMAIN + "/v3/product/info/list";

    /**
     * 产品价格报告
     */
    public static final String OZON_PRODUCT_PRICE_INFO = OZON_DOMAIN + "/v4/product/info/prices";

    /**
     * 创建新的聊天窗口
     */
    public static final String OZON_CHAT_START = OZON_DOMAIN + "/v1/chat/start";

    /**
     * 发送文字
     */
    public static final String OZON_CHAT_SEND_MESSAGE = OZON_DOMAIN + "/v1/chat/send/message";

    public static final String OZON_AD_DOMAIN = "https://api-performance.ozon.ru";

    /**
     * 广告token
     */
    public static final String OZON_AD_TOKEN = OZON_AD_DOMAIN + "/api/client/token";

    /**
     * 广告费用列表，产生费用的模板推广
     */
    public static final String OZON_AD_EXPENSE_LIST = OZON_AD_DOMAIN + "/api/client/statistics/expense/json";

    /**
     * 广告每日费用，按找传入的时间范围范围每日的费用
     */
    public static final String OZON_AD_DAILY = OZON_AD_DOMAIN + "/api/client/statistics/daily/json";

    /**
     * 广告数据统计，活动+Sku维度，返回的是报告UUID，需要再次报告详情接口（现查报告状态，成功后才能获取详情）
     */
    public static final String OZON_AD_STATISTICS = OZON_AD_DOMAIN + "/api/client/statistics/json";

    /**
     * 广告数据详情，通过报告UUID获取报告详情
     */
    public static final String OZON_AD_REPORT_DETAIL = OZON_AD_DOMAIN + "/api/client/statistics/report";

    /**
     * 广告报告状态
     */
    public static final String OZON_AD_REPORT_STATE = OZON_AD_DOMAIN + "/api/client/statistics/";

    /**
     * 仓库列表
     */
    public static final String OZON_WAREHOUSE_LIST = OZON_DOMAIN + "/v1/warehouse/list";
}
